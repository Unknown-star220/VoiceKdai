package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.IntentType
import com.example.data.model.ParsedItem
import com.example.data.model.SafetyGatePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object GeminiService {
    private const val TAG = "VoiceKadaiGemini"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_PROMPT = """
You are VoiceKadai NLU Engine, an AI voice transaction parser built for Indian small business owners (kirana stores, hardware shops, mechanics).
You parse spoken English, Tamil, Hindi, Tanglish (Tamil+English, e.g., 'Kumar kitta 5000 balance irukku', 'Ramesh 200 tea podi kudutharu'), and Hinglish.

Extract structured business intent into strict JSON with the following schema:
{
  "intent": "CREATE_SALE" | "RECORD_PAYMENT" | "CREATE_EXPENSE" | "CREATE_REMINDER" | "QUERY_BUSINESS_DATA",
  "customerName": string or null,
  "amount": number,
  "isCredit": boolean (true for Udhaar / given on credit / customer balance, false for cash received),
  "paymentMode": "CASH" | "UPI" | "CREDIT",
  "expenseCategory": "STOCK" | "RENT" | "ELECTRICITY" | "TRANSPORT" | "CHAI_SNACKS" | "STAFF_SALARY" | "MAINTENANCE" | "OTHER" | null,
  "items": [{"name": string, "quantity": number, "unit": string, "price": number}],
  "dueDateDaysFromNow": number or null (e.g. tomorrow = 1, next week = 7),
  "note": string,
  "summaryEnglish": string,
  "summaryRegional": string (short Tanglish / Hinglish / Tamil verification note)
}

Rules:
1. ZERO Financial Hallucination. If amount is missing, set amount to 0.0.
2. Tanglish examples:
- "Kumar kitta 5,000 balance irukku" -> intent: CREATE_SALE, customerName: "Kumar", amount: 5000, isCredit: true, paymentMode: "CREDIT", note: "Pending Balance"
- "Ramesh 200 tea podi kudutharu" / "Ramesh 200 tea podi udhaar" -> intent: CREATE_SALE, customerName: "Ramesh", amount: 200, items: [{"name": "Tea Podi", "quantity": 1, "price": 200}], isCredit: true
- "Murugan 1000 kudutharu" / "Murugan paid 1000" -> intent: RECORD_PAYMENT, customerName: "Murugan", amount: 1000, isCredit: false, paymentMode: "CASH"
- "Current bill 1450 kattiyaachu" / "Electricity bill 1450" -> intent: CREATE_EXPENSE, expenseCategory: "ELECTRICITY", amount: 1450, note: "EB Bill"
- "Deepak kitta 1500 vasul pannanum naalaiki" -> intent: CREATE_REMINDER, customerName: "Deepak", amount: 1500, dueDateDaysFromNow: 1
- "Today total sales evalo?" / "Who owes me money?" -> intent: QUERY_BUSINESS_DATA
Output strictly pure JSON with no markdown wrapping.
"""

    suspend fun parseVoiceInput(transcript: String): SafetyGatePayload = withContext(Dispatchers.IO) {
        val cleanedTranscript = transcript.trim()
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val jsonRequest = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", "$SYSTEM_PROMPT\n\nUser Voice Input: \"$cleanedTranscript\"") })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.1)
                        put("responseMimeType", "application/json")
                    })
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = jsonRequest.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("$BASE_URL?key=$apiKey")
                    .post(body)
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    val rootJson = JSONObject(responseBody)
                    val candidates = rootJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        val text = parts?.getJSONObject(0)?.optString("text")
                        if (!text.isNullOrBlank()) {
                            val parsedJson = JSONObject(text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim())
                            return@withContext parseJsonToPayload(parsedJson, cleanedTranscript)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API error, falling back to local deterministic NLU: ${e.message}")
            }
        }

        // Offline / Deterministic Hybrid Fallback Engine
        return@withContext parseLocally(cleanedTranscript)
    }

    private fun parseJsonToPayload(json: JSONObject, rawTranscript: String): SafetyGatePayload {
        val intentStr = json.optString("intent", "UNKNOWN")
        val intent = try { IntentType.valueOf(intentStr) } catch (e: Exception) { IntentType.UNKNOWN }
        val customerName = json.optString("customerName").takeIf { it.isNotBlank() && it != "null" }
        val amount = json.optDouble("amount", 0.0)
        val isCredit = json.optBoolean("isCredit", false)
        val paymentMode = json.optString("paymentMode", if (isCredit) "CREDIT" else "CASH")
        val expenseCategory = json.optString("expenseCategory").takeIf { it.isNotBlank() && it != "null" }
        val note = json.optString("note", "")
        val summaryEnglish = json.optString("summaryEnglish", "")
        val summaryRegional = json.optString("summaryRegional", "")
        val days = json.optInt("dueDateDaysFromNow", 0)

        val dueDate = if (days > 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, days)
            cal.timeInMillis
        } else null

        val itemsList = mutableListOf<ParsedItem>()
        val itemsArray = json.optJSONArray("items")
        if (itemsArray != null) {
            for (i in 0 until itemsArray.length()) {
                val itemObj = itemsArray.getJSONObject(i)
                itemsList.add(
                    ParsedItem(
                        name = itemObj.optString("name", "Item"),
                        quantity = itemObj.optDouble("quantity", 1.0),
                        unit = itemObj.optString("unit", "pcs"),
                        price = itemObj.optDouble("price", 0.0)
                    )
                )
            }
        }

        return SafetyGatePayload(
            intent = intent,
            rawTranscript = rawTranscript,
            confidence = 0.96,
            customerName = customerName,
            amount = amount,
            isCredit = isCredit,
            paymentMode = paymentMode,
            expenseCategory = expenseCategory,
            note = note,
            items = itemsList,
            dueDate = dueDate,
            summaryEnglish = summaryEnglish.ifBlank { generateSummary(intent, customerName, amount, isCredit, expenseCategory) },
            summaryRegional = summaryRegional.ifBlank { generateRegionalSummary(intent, customerName, amount, isCredit) }
        )
    }

    /**
     * Local Deterministic NLU for Indian Merchant Idioms (Tanglish, Hinglish, English, Tamil).
     * Ensures instant responsiveness & complete offline reliability.
     */
    fun parseLocally(transcript: String): SafetyGatePayload {
        val lower = transcript.lowercase().trim()
        val amount = extractAmount(lower)

        // 1. Query detection
        if (lower.contains("evalo") || lower.contains("how much") || lower.contains("kitna") || 
            lower.contains("who owes") || lower.contains("total sales") || lower.contains("profit") || 
            lower.contains("report") || lower.contains("summary") || lower.contains("balance list") ||
            lower.contains("vasul list") || lower.endsWith("?")) {
            return SafetyGatePayload(
                intent = IntentType.QUERY_BUSINESS_DATA,
                rawTranscript = transcript,
                confidence = 0.94,
                amount = 0.0,
                summaryEnglish = "Business Analytics & Report Query",
                summaryRegional = "வணிக அறிக்கை வினவல் (Business Query)"
            )
        }

        // 2. Reminder detection (e.g. "vasul pannanum", "remind", "naalaiki", "reminder")
        if (lower.contains("vasul") || lower.contains("remind") || lower.contains("collect") || lower.contains("due date") || lower.contains("naalaiki")) {
            val name = extractName(lower, listOf("vasul", "pannanum", "remind", "kitta", "from", "naalaiki", "tomorrow"))
            val days = if (lower.contains("naalaiki") || lower.contains("tomorrow") || lower.contains("kal")) 1 else 3
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, days) }
            return SafetyGatePayload(
                intent = IntentType.CREATE_REMINDER,
                rawTranscript = transcript,
                confidence = 0.92,
                customerName = name ?: "Customer",
                amount = amount,
                dueDate = cal.timeInMillis,
                note = "Payment Reminder for ₹$amount",
                summaryEnglish = "Create Payment Reminder for ${name ?: "Customer"} (₹$amount)",
                summaryRegional = "${name ?: "Customer"} க்கு ₹$amount நினைவூட்டல் அமைக்கவும்"
            )
        }

        // 3. Expense detection (e.g. "electricity", "eb bill", "rent", "petrol", "chai", "tea", "salary", "kharch", "selavu")
        val isExpense = lower.contains("bill") || lower.contains("rent") || lower.contains("electric") || 
                lower.contains("eb ") || lower.contains("current bill") || lower.contains("transport") || 
                lower.contains("petrol") || lower.contains("diesel") || lower.contains("chai") || 
                lower.contains("tea") || lower.contains("salary") || lower.contains("sampalam") || 
                lower.contains("selavu") || lower.contains("kharch") || lower.contains("expense")

        if (isExpense) {
            val category = when {
                lower.contains("electric") || lower.contains("eb") || lower.contains("current") -> "ELECTRICITY"
                lower.contains("rent") || lower.contains("vaadagai") -> "RENT"
                lower.contains("petrol") || lower.contains("transport") || lower.contains("auto") || lower.contains("diesel") -> "TRANSPORT"
                lower.contains("chai") || lower.contains("tea") || lower.contains("snacks") || lower.contains("coffee") -> "CHAI_SNACKS"
                lower.contains("salary") || lower.contains("sampalam") || lower.contains("staff") -> "STAFF_SALARY"
                lower.contains("stock") || lower.contains("samaan") || lower.contains("goods") -> "STOCK"
                else -> "OTHER"
            }
            return SafetyGatePayload(
                intent = IntentType.CREATE_EXPENSE,
                rawTranscript = transcript,
                confidence = 0.95,
                amount = amount,
                expenseCategory = category,
                note = transcript,
                summaryEnglish = "Record Expense: $category ₹$amount",
                summaryRegional = "$category செலவு ₹$amount பதிவு செய்யப்படுகிறது"
            )
        }

        // 4. Payment Received (Jama / Customer paid back, e.g. "kudutharu", "paid", "jama", "settle", "received")
        val isPaymentReceived = lower.contains("kudutharu") || lower.contains("paid") || lower.contains("jama") || 
                lower.contains("received") || lower.contains("settled") || lower.contains("pay pannaaru") || 
                lower.contains("thantanga") || lower.contains("diya")

        if (isPaymentReceived && !lower.contains("udhaar") && !lower.contains("credit") && !lower.contains("balance")) {
            val name = extractName(lower, listOf("kudutharu", "paid", "jama", "received", "settled", "pay", "thantanga", "diya", "rupees", "rs", "₹"))
            return SafetyGatePayload(
                intent = IntentType.RECORD_PAYMENT,
                rawTranscript = transcript,
                confidence = 0.94,
                customerName = name ?: "Customer",
                amount = amount,
                isCredit = false,
                paymentMode = if (lower.contains("upi") || lower.contains("gpay") || lower.contains("phonepe")) "UPI" else "CASH",
                note = "Payment Received (Jama)",
                summaryEnglish = "Record Payment Received from ${name ?: "Customer"}: ₹$amount",
                summaryRegional = "${name ?: "Customer"} இடம் ₹$amount பணம் வரவு வைக்கப்பட்டது (Got ₹)"
            )
        }

        // 5. Sale / Credit / Udhaar (e.g. "Kumar kitta 5000 balance irukku", "Ramesh 200 udhaar", "sale cash 450")
        val isCredit = lower.contains("balance") || lower.contains("irukku") || lower.contains("udhaar") || 
                lower.contains("credit") || lower.contains("kitta") || lower.contains("baaki") || lower.contains("dharr")
        val name = extractName(lower, listOf("kitta", "balance", "irukku", "udhaar", "credit", "baaki", "sale", "cash", "rs", "₹"))

        val items = mutableListOf<ParsedItem>()
        if (lower.contains("tea") || lower.contains("rice") || lower.contains("sugar") || lower.contains("oil") || lower.contains("dal") || lower.contains("cement")) {
            val itemName = when {
                lower.contains("tea") -> "Tea Podi"
                lower.contains("rice") -> "Rice Bag"
                lower.contains("sugar") -> "Sugar (Chini)"
                lower.contains("oil") -> "Cooking Oil"
                lower.contains("dal") -> "Toor Dal"
                lower.contains("cement") -> "Cement Bag"
                else -> "Item"
            }
            items.add(ParsedItem(name = itemName, quantity = 1.0, price = amount))
        }

        return SafetyGatePayload(
            intent = IntentType.CREATE_SALE,
            rawTranscript = transcript,
            confidence = 0.93,
            customerName = name ?: if (isCredit) "Customer" else null,
            amount = amount,
            isCredit = isCredit,
            paymentMode = if (isCredit) "CREDIT" else "CASH",
            items = items,
            note = if (isCredit) "Udhaar Credit Sale" else "Cash Sale",
            summaryEnglish = if (isCredit) "Add Credit (Udhaar) of ₹$amount for ${name ?: "Customer"}" else "Record Cash Sale of ₹$amount",
            summaryRegional = if (isCredit) "${name ?: "Customer"} க்கு கடன் (Udhaar) ₹$amount பற்று வைக்கப்படுகிறது" else "ரொக்க விற்பனை ₹$amount பதிவு செய்யப்படுகிறது"
        )
    }

    private fun extractAmount(text: String): Double {
        val matcher = Pattern.compile("(\\d+(?:,\\d+)*(?:\\.\\d+)?)").matcher(text)
        var lastAmount = 0.0
        while (matcher.find()) {
            val numStr = matcher.group(1)?.replace(",", "")
            val parsed = numStr?.toDoubleOrNull()
            if (parsed != null && parsed > 0) {
                lastAmount = parsed
            }
        }
        return lastAmount
    }

    private fun extractName(text: String, stopWords: List<String>): String? {
        val words = text.split(" ").filter { it.isNotBlank() }
        for (w in words) {
            val clean = w.replace(Regex("[^a-zA-Z]"), "").trim()
            if (clean.length >= 3 && clean.lowercase() !in stopWords &&
                !clean.equals("sale", true) && !clean.equals("cash", true) && 
                !clean.equals("today", true) && !clean.equals("item", true) &&
                !clean.equals("bill", true)) {
                return clean.replaceFirstChar { it.uppercase() }
            }
        }
        return null
    }

    private fun generateSummary(intent: IntentType, name: String?, amount: Double, isCredit: Boolean, category: String?): String {
        return when (intent) {
            IntentType.CREATE_SALE -> if (isCredit) "Credit Sale (Udhaar): ₹$amount for ${name ?: "Customer"}" else "Cash Sale: ₹$amount"
            IntentType.RECORD_PAYMENT -> "Payment Received: ₹$amount from ${name ?: "Customer"}"
            IntentType.CREATE_EXPENSE -> "Expense: ${category ?: "Business"} of ₹$amount"
            IntentType.CREATE_REMINDER -> "Payment Due Reminder: ₹$amount for ${name ?: "Customer"}"
            IntentType.QUERY_BUSINESS_DATA -> "Business Report Analytics"
            IntentType.UNKNOWN -> "Transaction of ₹$amount"
        }
    }

    private fun generateRegionalSummary(intent: IntentType, name: String?, amount: Double, isCredit: Boolean): String {
        return when (intent) {
            IntentType.CREATE_SALE -> if (isCredit) "${name ?: "வாடிக்கையாளர்"} பற்று (Gave): ₹$amount" else "ரொக்க விற்பனை: ₹$amount"
            IntentType.RECORD_PAYMENT -> "${name ?: "வாடிக்கையாளர்"} வரவு (Got): ₹$amount"
            IntentType.CREATE_EXPENSE -> "செலவு பதிவு: ₹$amount"
            IntentType.CREATE_REMINDER -> "நினைவூட்டல்: ₹$amount"
            IntentType.QUERY_BUSINESS_DATA -> "கணக்கு விவரங்கள் வினவல்"
            IntentType.UNKNOWN -> "பரிவர்த்தனை"
        }
    }
}
