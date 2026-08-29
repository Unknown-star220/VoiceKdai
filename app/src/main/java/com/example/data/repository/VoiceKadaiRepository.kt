package com.example.data.repository

import com.example.data.local.dao.VoiceKadaiDao
import com.example.data.local.entities.*
import com.example.data.model.IntentType
import com.example.data.model.QueryAnalysisResult
import com.example.data.model.SafetyGatePayload
import com.example.data.remote.GeminiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID

class VoiceKadaiRepository(private val dao: VoiceKadaiDao) {

    val businessFlow: Flow<BusinessEntity?> = dao.getBusinessFlow()
    val customersFlow: Flow<List<CustomerEntity>> = dao.getAllCustomers()
    val transactionsFlow: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val expensesFlow: Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    val remindersFlow: Flow<List<ReminderEntity>> = dao.getAllReminders()
    val aiMessagesFlow: Flow<List<AiMessageEntity>> = dao.getAllAiMessages()
    val totalUdhaarFlow: Flow<Double> = dao.getTotalUdhaarToReceive()
    val totalAdvanceFlow: Flow<Double> = dao.getTotalAdvanceToGive()

    fun getTodayStartTimestamp(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    val todaySalesFlow: Flow<Double> = dao.getTodaySales(getTodayStartTimestamp())
    val todayExpensesFlow: Flow<Double> = dao.getTodayExpenses(getTodayStartTimestamp())

    companion object {
        val DEVELOPER_EMAILS = setOf(
            "safiya.umar13@gmail.com",
            "Safiya.umar13@gmail.com".lowercase()
        )

        fun isDeveloperEmail(email: String?): Boolean {
            if (email.isNullOrBlank()) return false
            return DEVELOPER_EMAILS.contains(email.trim().lowercase())
        }
    }

    suspend fun initializeSeedDataIfNeeded() = withContext(Dispatchers.IO) {
        val existingBiz = dao.getBusiness()
        if (existingBiz == null) {
            val biz = BusinessEntity(
                id = "biz_default",
                name = "My Kirana Store",
                ownerName = "Merchant",
                category = "Kirana & Provision",
                phone = "",
                language = "Tanglish",
                currencySymbol = "₹",
                planTier = "FREE",
                dailyVoiceCount = 0,
                maxDailyVoiceQuota = 5,
                isSignedIn = false,
                userEmail = "",
                userDisplayName = "Merchant"
            )
            dao.insertBusiness(biz)

            // Seed AI welcome message only
            val now = System.currentTimeMillis()
            dao.insertAiMessage(
                AiMessageEntity(
                    id = "msg_init",
                    role = "assistant",
                    content = "Vanakkam! I am your VoiceKadai Business Assistant. Speak in Tamil, English, or Tanglish (e.g. 'Kumar kitta 5000 balance irukku', 'Today sales evalo?').",
                    timestamp = now
                )
            )
        }
    }

    suspend fun parseVoice(transcript: String): SafetyGatePayload {
        return GeminiService.parseVoiceInput(transcript)
    }

    suspend fun executeSafetyGateMutation(payload: SafetyGatePayload): String = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val biz = dao.getBusiness() ?: BusinessEntity()

        if (biz.planTier == "FREE" && biz.dailyVoiceCount >= biz.maxDailyVoiceQuota) {
            return@withContext "⚠️ Free Plan Limit Reached (${biz.dailyVoiceCount}/${biz.maxDailyVoiceQuota} entries). Upgrade to PRO via In-App Purchases for 100 daily entries!"
        }

        // Increment voice count
        dao.updateBusiness(biz.copy(dailyVoiceCount = biz.dailyVoiceCount + 1))

        when (payload.intent) {
            IntentType.CREATE_SALE -> {
                val customerName = payload.customerName?.trim()
                var customerId: String? = null

                if (payload.isCredit && !customerName.isNullOrBlank()) {
                    var customer = dao.findCustomerByName(customerName)
                    if (customer == null) {
                        customer = CustomerEntity(
                            id = "cust_" + UUID.randomUUID().toString().take(8),
                            name = customerName,
                            phone = payload.customerPhone ?: "",
                            currentBalance = payload.amount,
                            lastTransactionAt = now,
                            notes = "Auto-created via Voice Kadai"
                        )
                        dao.insertCustomer(customer)
                    } else {
                        // Deterministic DB calculation: newBalance = currentBalance + amount
                        val updated = customer.copy(
                            currentBalance = customer.currentBalance + payload.amount,
                            lastTransactionAt = now
                        )
                        dao.updateCustomer(updated)
                    }
                    customerId = customer.id
                }

                val tx = TransactionEntity(
                    id = "tx_" + UUID.randomUUID().toString().take(8),
                    customerId = customerId,
                    customerName = customerName ?: "Walk-in Customer",
                    type = if (payload.isCredit) "SALE_CREDIT" else "SALE_CASH",
                    amount = payload.amount,
                    paymentMode = payload.paymentMode,
                    note = payload.note.ifBlank { if (payload.isCredit) "Udhaar Credit Sale" else "Cash Sale" },
                    audioTranscript = payload.rawTranscript,
                    createdAt = now
                )
                dao.insertTransaction(tx)
                return@withContext if (payload.isCredit) "✅ Recorded ₹${payload.amount} Udhaar (Credit) for ${customerName}!" else "✅ Recorded ₹${payload.amount} Cash Sale!"
            }

            IntentType.RECORD_PAYMENT -> {
                val customerName = payload.customerName?.trim() ?: "Customer"
                var customer = dao.findCustomerByName(customerName)
                if (customer == null) {
                    customer = CustomerEntity(
                        id = "cust_" + UUID.randomUUID().toString().take(8),
                        name = customerName,
                        phone = payload.customerPhone ?: "",
                        currentBalance = -payload.amount, // Advance
                        lastTransactionAt = now
                    )
                    dao.insertCustomer(customer)
                } else {
                    // Deterministic DB calculation: newBalance = currentBalance - amount
                    val updated = customer.copy(
                        currentBalance = customer.currentBalance - payload.amount,
                        lastTransactionAt = now
                    )
                    dao.updateCustomer(updated)
                }

                val tx = TransactionEntity(
                    id = "tx_" + UUID.randomUUID().toString().take(8),
                    customerId = customer.id,
                    customerName = customerName,
                    type = "PAYMENT_RECEIVED",
                    amount = payload.amount,
                    paymentMode = payload.paymentMode,
                    note = payload.note.ifBlank { "Payment Received (Jama)" },
                    audioTranscript = payload.rawTranscript,
                    createdAt = now
                )
                dao.insertTransaction(tx)
                return@withContext "✅ Payment of ₹${payload.amount} received from $customerName. Balance updated to ₹${customer.currentBalance - payload.amount}."
            }

            IntentType.CREATE_EXPENSE -> {
                val category = payload.expenseCategory ?: "OTHER"
                val expense = ExpenseEntity(
                    id = "exp_" + UUID.randomUUID().toString().take(8),
                    category = category,
                    amount = payload.amount,
                    note = payload.note.ifBlank { "$category expense" },
                    paymentMode = payload.paymentMode,
                    createdAt = now
                )
                dao.insertExpense(expense)
                return@withContext "✅ Recorded $category expense of ₹${payload.amount}."
            }

            IntentType.CREATE_REMINDER -> {
                val customerName = payload.customerName?.trim() ?: "Customer"
                var customer = dao.findCustomerByName(customerName)
                val customerId = customer?.id ?: ("cust_" + UUID.randomUUID().toString().take(8))
                if (customer == null) {
                    dao.insertCustomer(
                        CustomerEntity(
                            id = customerId,
                            name = customerName,
                            currentBalance = payload.amount,
                            lastTransactionAt = now
                        )
                    )
                }
                val reminder = ReminderEntity(
                    id = "rem_" + UUID.randomUUID().toString().take(8),
                    customerId = customerId,
                    customerName = customerName,
                    amount = payload.amount,
                    dueDate = payload.dueDate ?: (now + 86400000L),
                    status = "PENDING",
                    note = payload.note.ifBlank { "Payment collection reminder" },
                    createdAt = now
                )
                dao.insertReminder(reminder)
                return@withContext "✅ Smart Reminder created for $customerName for ₹${payload.amount}."
            }

            IntentType.QUERY_BUSINESS_DATA, IntentType.UNKNOWN -> {
                return@withContext "Processed business query."
            }
        }
    }

    suspend fun executeBusinessAnalyticsQuery(question: String): QueryAnalysisResult = withContext(Dispatchers.IO) {
        val customers = dao.getAllCustomers().first()
        val transactions = dao.getAllTransactions().first()
        val expenses = dao.getAllExpenses().first()

        val totalUdhaar = customers.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }
        val topDebtor = customers.maxByOrNull { it.currentBalance }
        val todayStart = getTodayStartTimestamp()
        val todaySales = transactions.filter { (it.type == "SALE_CASH" || it.type == "SALE_CREDIT") && it.createdAt >= todayStart }.sumOf { it.amount }
        val todayCashIn = transactions.filter { it.type == "PAYMENT_RECEIVED" && it.createdAt >= todayStart }.sumOf { it.amount }
        val totalExpenses = expenses.sumOf { it.amount }
        val todayExpenses = expenses.filter { it.createdAt >= todayStart }.sumOf { it.amount }

        val qLower = question.lowercase()

        val (title, calculatedValue, breakdownText, followUp) = when {
            qLower.contains("who owes") || qLower.contains("top debtor") || qLower.contains("vasul") || qLower.contains("udhaar list") -> {
                val debtors = customers.filter { it.currentBalance > 0 }.sortedByDescending { it.currentBalance }
                val breakdown = debtors.take(4).joinToString("\n") { "• ${it.name}: ₹${String.format("%.0f", it.currentBalance)}" }
                QueryAnalysisResult(
                    title = "Pending Udhaar Collections (கடன் வசூல்)",
                    calculatedValue = "₹${String.format("%.0f", totalUdhaar)} Total Pending",
                    breakdownText = if (debtors.isNotEmpty()) "Top pending balances:\n$breakdown" else "All customer balances are fully settled! 🎉",
                    suggestedFollowUp = "Send WhatsApp reminder to ${topDebtor?.name ?: "customers"}"
                )
            }
            qLower.contains("profit") || qLower.contains("net") || qLower.contains("margin") -> {
                val net = (todaySales + todayCashIn) - todayExpenses
                QueryAnalysisResult(
                    title = "Today's Net Cash & Position",
                    calculatedValue = "₹${String.format("%.0f", net)}",
                    breakdownText = "• Today's Sales: ₹${String.format("%.0f", todaySales)}\n• Cash Inflow: ₹${String.format("%.0f", todayCashIn)}\n• Today's Expenses: ₹${String.format("%.0f", todayExpenses)}",
                    suggestedFollowUp = "Review expense breakdown"
                )
            }
            qLower.contains("expense") || qLower.contains("selavu") || qLower.contains("bill") || qLower.contains("kharch") -> {
                val byCat = expenses.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
                val breakdown = byCat.entries.joinToString("\n") { "• ${it.key}: ₹${String.format("%.0f", it.value)}" }
                QueryAnalysisResult(
                    title = "Total Business Expenses (மொத்த செலவுகள்)",
                    calculatedValue = "₹${String.format("%.0f", totalExpenses)}",
                    breakdownText = "Categorized expenses:\n$breakdown",
                    suggestedFollowUp = "Record electricity or transport expense"
                )
            }
            else -> {
                // General Business Summary
                QueryAnalysisResult(
                    title = "Business Overview Summary",
                    calculatedValue = "₹${String.format("%.0f", todaySales)} Today's Sales",
                    breakdownText = "• Pending Udhaar (You'll Get): ₹${String.format("%.0f", totalUdhaar)}\n• Active Customers: ${customers.size}\n• Today Expenses: ₹${String.format("%.0f", todayExpenses)}",
                    suggestedFollowUp = "Who owes me the most money?"
                )
            }
        }

        // Record message in history
        val now = System.currentTimeMillis()
        dao.insertAiMessage(AiMessageEntity(id = "user_$now", role = "user", content = question, timestamp = now))
        dao.insertAiMessage(AiMessageEntity(id = "ai_$now", role = "assistant", content = "$title: $calculatedValue\n\n$breakdownText", timestamp = now + 1))

        return@withContext QueryAnalysisResult(title, calculatedValue, breakdownText, followUp)
    }

    suspend fun recordManualCustomerTransaction(
        customerId: String,
        customerName: String,
        amount: Double,
        isGaveCredit: Boolean, // true = Gave ₹ (Udhaar), false = Got ₹ (Jama)
        note: String
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val customer = dao.getCustomerById(customerId) ?: return@withContext

        val updatedBalance = if (isGaveCredit) {
            customer.currentBalance + amount
        } else {
            customer.currentBalance - amount
        }

        dao.updateCustomer(customer.copy(currentBalance = updatedBalance, lastTransactionAt = now))

        val tx = TransactionEntity(
            id = "tx_" + UUID.randomUUID().toString().take(8),
            customerId = customerId,
            customerName = customerName,
            type = if (isGaveCredit) "SALE_CREDIT" else "PAYMENT_RECEIVED",
            amount = amount,
            paymentMode = if (isGaveCredit) "CREDIT" else "CASH",
            note = note.ifBlank { if (isGaveCredit) "Gave credit (Udhaar)" else "Got payment (Jama)" },
            createdAt = now
        )
        dao.insertTransaction(tx)
    }

    suspend fun addNewCustomer(name: String, phone: String, initialBalance: Double, notes: String) = withContext(Dispatchers.IO) {
        val id = "cust_" + UUID.randomUUID().toString().take(8)
        val customer = CustomerEntity(
            id = id,
            name = name.trim(),
            phone = phone.trim(),
            currentBalance = initialBalance,
            lastTransactionAt = System.currentTimeMillis(),
            notes = notes.trim()
        )
        dao.insertCustomer(customer)
        if (initialBalance != 0.0) {
            dao.insertTransaction(
                TransactionEntity(
                    id = "tx_" + UUID.randomUUID().toString().take(8),
                    customerId = id,
                    customerName = name,
                    type = if (initialBalance > 0) "SALE_CREDIT" else "PAYMENT_RECEIVED",
                    amount = Math.abs(initialBalance),
                    paymentMode = "CREDIT",
                    note = "Opening Balance",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteCustomer(customerId: String) = withContext(Dispatchers.IO) {
        dao.deleteCustomer(customerId)
    }

    suspend fun addExpense(category: String, amount: Double, note: String, paymentMode: String) = withContext(Dispatchers.IO) {
        val expense = ExpenseEntity(
            id = "exp_" + UUID.randomUUID().toString().take(8),
            category = category,
            amount = amount,
            note = note,
            paymentMode = paymentMode,
            createdAt = System.currentTimeMillis()
        )
        dao.insertExpense(expense)
    }

    suspend fun deleteExpense(id: String) = withContext(Dispatchers.IO) {
        dao.deleteExpense(id)
    }

    suspend fun updateReminderStatus(id: String, status: String) = withContext(Dispatchers.IO) {
        dao.updateReminderStatus(id, status)
    }

    suspend fun deleteReminder(id: String) = withContext(Dispatchers.IO) {
        dao.deleteReminder(id)
    }

    val paymentsFlow: Flow<List<SubscriptionPaymentEntity>> = dao.getAllPayments()

    suspend fun updateLanguage(language: String) = withContext(Dispatchers.IO) {
        val currentBiz = dao.getBusiness() ?: BusinessEntity()
        dao.insertBusiness(currentBiz.copy(language = language))
    }

    suspend fun signInWithProvider(
        provider: String,
        email: String,
        displayName: String,
        businessName: String? = null,
        phone: String? = null
    ) = withContext(Dispatchers.IO) {
        val currentBiz = dao.getBusiness() ?: BusinessEntity()
        
        // Check if this specific email has an existing active paid subscription
        val isExistingPaidPlan = currentBiz.userEmail.equals(email.trim(), ignoreCase = true) && 
                (currentBiz.planTier == "PRO" || currentBiz.planTier == "BUSINESS") &&
                (currentBiz.subscriptionExpiry > System.currentTimeMillis())
        
        val assignedTier = if (isExistingPaidPlan) currentBiz.planTier else "FREE"
        val assignedQuota = if (isExistingPaidPlan) currentBiz.maxDailyVoiceQuota else 50
        val assignedExpiry = if (isExistingPaidPlan) currentBiz.subscriptionExpiry else 0L

        val updated = currentBiz.copy(
            isSignedIn = true,
            authProvider = provider,
            userEmail = email.trim(),
            userDisplayName = displayName.trim(),
            name = businessName?.ifBlank { currentBiz.name } ?: currentBiz.name,
            phone = phone?.ifBlank { currentBiz.phone } ?: currentBiz.phone,
            ownerName = displayName.trim().ifBlank { currentBiz.ownerName },
            planTier = assignedTier,
            maxDailyVoiceQuota = assignedQuota,
            subscriptionExpiry = assignedExpiry
        )
        dao.insertBusiness(updated)
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        val currentBiz = dao.getBusiness() ?: BusinessEntity()
        val updated = currentBiz.copy(
            isSignedIn = false,
            userEmail = "",
            userDisplayName = "Merchant",
            planTier = "FREE",
            maxDailyVoiceQuota = 5,
            subscriptionExpiry = 0L
        )
        dao.insertBusiness(updated)
    }

    suspend fun processSubscriptionPayment(
        planTier: String,
        billingCycle: String,
        paymentMethod: String,
        transactionRef: String? = null
    ): SubscriptionPaymentEntity = withContext(Dispatchers.IO) {
        val baseAmount = when {
            planTier == "PRO" && billingCycle == "ANNUAL" -> 2499.0
            planTier == "PRO" -> 299.0
            planTier == "BUSINESS" && billingCycle == "ANNUAL" -> 7999.0
            else -> 999.0
        }
        val taxAmount = (baseAmount * 0.18 * 100).toInt() / 100.0 // 18% GST in India
        val totalPaid = baseAmount + taxAmount
        val orderId = "ORD_VK_" + (100000..999999).random()
        val finalTxRef = transactionRef ?: ("TXN_" + System.currentTimeMillis().toString().takeLast(8))
        val invoiceNo = "INV-2026-VK-" + (1000..9999).random()

        val paymentEntity = SubscriptionPaymentEntity(
            orderId = orderId,
            planTier = planTier,
            billingCycle = billingCycle,
            amount = baseAmount,
            taxAmount = taxAmount,
            totalPaid = totalPaid,
            paymentMethod = paymentMethod,
            transactionRef = finalTxRef,
            status = "SUCCESS",
            invoiceNumber = invoiceNo,
            createdAt = System.currentTimeMillis()
        )
        dao.insertPayment(paymentEntity)

        val durationMillis = if (billingCycle == "ANNUAL") 365L * 24 * 3600 * 1000 else 30L * 24 * 3600 * 1000
        val currentBiz = dao.getBusiness() ?: BusinessEntity()
        val newQuota = if (planTier == "BUSINESS") 9999 else 100
        val updatedBiz = currentBiz.copy(
            planTier = planTier,
            billingCycle = billingCycle,
            maxDailyVoiceQuota = newQuota,
            subscriptionExpiry = System.currentTimeMillis() + durationMillis,
            lastPaymentOrderId = orderId,
            lastPaymentAmount = totalPaid
        )
        dao.insertBusiness(updatedBiz)
        paymentEntity
    }

    suspend fun clearAiChat() = withContext(Dispatchers.IO) {
        dao.clearAiMessages()
    }
}

