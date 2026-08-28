package com.example.data.model

enum class IntentType {
    CREATE_SALE,
    RECORD_PAYMENT,
    CREATE_EXPENSE,
    CREATE_REMINDER,
    QUERY_BUSINESS_DATA,
    UNKNOWN
}

data class ParsedItem(
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "pcs",
    val price: Double = 0.0
)

data class SafetyGatePayload(
    val intent: IntentType,
    val rawTranscript: String,
    val confidence: Double = 0.95,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val amount: Double = 0.0,
    val isCredit: Boolean = false, // true = Udhaar (given on credit), false = Cash received
    val paymentMode: String = "CASH", // CASH, UPI, CREDIT
    val expenseCategory: String? = null,
    val note: String = "",
    val items: List<ParsedItem> = emptyList(),
    val dueDate: Long? = null,
    val queryType: String? = null,
    val summaryEnglish: String = "",
    val summaryRegional: String = ""
)

data class QueryAnalysisResult(
    val title: String,
    val calculatedValue: String,
    val breakdownText: String,
    val suggestedFollowUp: String? = null
)
