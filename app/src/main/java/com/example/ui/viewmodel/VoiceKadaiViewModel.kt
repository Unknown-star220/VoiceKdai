package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.data.model.IntentType
import com.example.data.model.QueryAnalysisResult
import com.example.data.model.SafetyGatePayload
import com.example.data.repository.VoiceKadaiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    AUTH,
    DASHBOARD,
    CUSTOMERS,
    CUSTOMER_DETAIL,
    EXPENSES,
    REMINDERS,
    AI_ANALYTICS,
    BLUEPRINT_SPECS, // Complete 12-Part System Architecture & Specifications Explorer!
    SUBSCRIPTION,
    SETTINGS
}

data class CheckoutPlan(
    val planTier: String = "PRO",
    val billingCycle: String = "MONTHLY",
    val baseAmount: Double = 299.0,
    val taxAmount: Double = 53.82,
    val totalAmount: Double = 352.82
)

data class UiState(
    val selectedScreen: AppScreen = AppScreen.DASHBOARD,
    val selectedCustomerId: String? = null,
    val isRecording: Boolean = false,
    val isProcessingVoice: Boolean = false,
    val liveTranscript: String = "",
    val activeSafetyGatePayload: SafetyGatePayload? = null,
    val activeCheckoutPlan: CheckoutPlan? = null,
    val isProcessingPayment: Boolean = false,
    val lastCompletedPayment: SubscriptionPaymentEntity? = null,
    val userFeedbackMessage: String? = null,
    val searchQuery: String = "",
    val customerFilter: String = "ALL", // ALL, UDHAAR, ADVANCE, SETTLED
    val activeBlueprintPart: Int = 1
)

class VoiceKadaiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VoiceKadaiRepository

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val business: StateFlow<BusinessEntity?>
    val customers: StateFlow<List<CustomerEntity>>
    val transactions: StateFlow<List<TransactionEntity>>
    val expenses: StateFlow<List<ExpenseEntity>>
    val reminders: StateFlow<List<ReminderEntity>>
    val aiMessages: StateFlow<List<AiMessageEntity>>
    val payments: StateFlow<List<SubscriptionPaymentEntity>>
    val totalUdhaar: StateFlow<Double>
    val totalAdvance: StateFlow<Double>
    val todaySales: StateFlow<Double>
    val todayExpenses: StateFlow<Double>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = VoiceKadaiRepository(db.voiceKadaiDao())

        business = repository.businessFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        customers = repository.customersFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        transactions = repository.transactionsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        expenses = repository.expensesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        reminders = repository.remindersFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        aiMessages = repository.aiMessagesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        payments = repository.paymentsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        totalUdhaar = repository.totalUdhaarFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
        totalAdvance = repository.totalAdvanceFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
        todaySales = repository.todaySalesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
        todayExpenses = repository.todayExpensesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        viewModelScope.launch {
            repository.initializeSeedDataIfNeeded()
        }
    }

    fun navigateTo(screen: AppScreen, customerId: String? = null) {
        _uiState.update { it.copy(selectedScreen = screen, selectedCustomerId = customerId) }
    }

    // --- Authentication Actions ---
    fun signInWithGoogle(
        email: String = "safiya.umar13@gmail.com",
        displayName: String = "Safiya Umar",
        storeName: String = "Sri Lakshmi Kirana & General Store",
        phone: String = "+91 98765 43210"
    ) {
        viewModelScope.launch {
            repository.signInWithProvider(
                provider = "GOOGLE",
                email = email,
                displayName = displayName,
                businessName = storeName,
                phone = phone
            )
            _uiState.update {
                it.copy(
                    selectedScreen = AppScreen.DASHBOARD,
                    userFeedbackMessage = "Signed in with Google as $displayName"
                )
            }
        }
    }

    fun signInWithMicrosoft(
        email: String = "merchant@outlook.com",
        displayName: String = "K. Ramanathan",
        storeName: String = "Ramanathan Wholesale Mart",
        phone: String = "+91 98401 23456"
    ) {
        viewModelScope.launch {
            repository.signInWithProvider(
                provider = "MICROSOFT",
                email = email,
                displayName = displayName,
                businessName = storeName,
                phone = phone
            )
            _uiState.update {
                it.copy(
                    selectedScreen = AppScreen.DASHBOARD,
                    userFeedbackMessage = "Signed in with Microsoft as $displayName"
                )
            }
        }
    }

    fun signInWithPhone(
        phoneNumber: String,
        displayName: String = "Shop Owner",
        storeName: String = "My Kirana Store"
    ) {
        viewModelScope.launch {
            repository.signInWithProvider(
                provider = "PHONE",
                email = "${phoneNumber.replace("+", "").replace(" ", "")}@voicekadai.in",
                displayName = displayName,
                businessName = storeName,
                phone = phoneNumber
            )
            _uiState.update {
                it.copy(
                    selectedScreen = AppScreen.DASHBOARD,
                    userFeedbackMessage = "Phone verified: $phoneNumber"
                )
            }
        }
    }

    fun signInAsGuest() {
        viewModelScope.launch {
            repository.signInWithProvider(
                provider = "GUEST",
                email = "guest.merchant@voicekadai.in",
                displayName = "Guest Merchant",
                businessName = "Demo Kirana Store",
                phone = "+91 90000 00000"
            )
            _uiState.update {
                it.copy(
                    selectedScreen = AppScreen.DASHBOARD,
                    userFeedbackMessage = "Welcome to Demo Store mode"
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _uiState.update {
                it.copy(
                    selectedScreen = AppScreen.AUTH,
                    userFeedbackMessage = "You have been signed out."
                )
            }
        }
    }

    // --- In-App Purchase & Real Money Subscriptions ---
    fun openPaymentCheckout(planTier: String, billingCycle: String = "MONTHLY") {
        val base = when {
            planTier == "PRO" && billingCycle == "ANNUAL" -> 2499.0
            planTier == "PRO" -> 299.0
            planTier == "BUSINESS" && billingCycle == "ANNUAL" -> 7999.0
            else -> 999.0
        }
        val tax = (base * 0.18 * 100).toInt() / 100.0
        val total = base + tax
        _uiState.update {
            it.copy(
                activeCheckoutPlan = CheckoutPlan(
                    planTier = planTier,
                    billingCycle = billingCycle,
                    baseAmount = base,
                    taxAmount = tax,
                    totalAmount = total
                )
            )
        }
    }

    fun closePaymentCheckout() {
        _uiState.update { it.copy(activeCheckoutPlan = null, isProcessingPayment = false) }
    }

    fun dismissPaymentSuccessDialog() {
        _uiState.update { it.copy(lastCompletedPayment = null) }
    }

    fun processRealPayment(
        paymentMethod: String,
        customTxnRef: String? = null
    ) {
        val checkout = _uiState.value.activeCheckoutPlan ?: return
        _uiState.update { it.copy(isProcessingPayment = true) }
        viewModelScope.launch {
            val payment = repository.processSubscriptionPayment(
                planTier = checkout.planTier,
                billingCycle = checkout.billingCycle,
                paymentMethod = paymentMethod,
                transactionRef = customTxnRef
            )
            _uiState.update {
                it.copy(
                    isProcessingPayment = false,
                    activeCheckoutPlan = null,
                    lastCompletedPayment = payment,
                    userFeedbackMessage = "Payment successful! ${checkout.planTier} Plan activated."
                )
            }
        }
    }

    fun setCustomerFilter(filter: String) {
        _uiState.update { it.copy(customerFilter = filter) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setBlueprintPart(partNumber: Int) {
        _uiState.update { it.copy(activeBlueprintPart = partNumber) }
    }

    fun dismissUserFeedback() {
        _uiState.update { it.copy(userFeedbackMessage = null) }
    }

    fun startVoiceRecording() {
        _uiState.update { it.copy(isRecording = true, liveTranscript = "", isProcessingVoice = false) }
    }

    fun updateLiveTranscript(transcript: String) {
        _uiState.update { it.copy(liveTranscript = transcript) }
    }

    fun stopAndProcessVoiceRecording(transcript: String) {
        _uiState.update { it.copy(isRecording = false, isProcessingVoice = true, liveTranscript = transcript) }
        viewModelScope.launch {
            val payload = repository.parseVoice(transcript)
            _uiState.update {
                it.copy(
                    isProcessingVoice = false,
                    activeSafetyGatePayload = payload
                )
            }
        }
    }

    fun triggerSampleVoicePrompt(sampleText: String) {
        _uiState.update { it.copy(liveTranscript = sampleText, isProcessingVoice = true) }
        viewModelScope.launch {
            val payload = repository.parseVoice(sampleText)
            _uiState.update {
                it.copy(
                    isProcessingVoice = false,
                    activeSafetyGatePayload = payload
                )
            }
        }
    }

    fun dismissSafetyGate() {
        _uiState.update { it.copy(activeSafetyGatePayload = null) }
    }

    fun confirmSafetyGateMutation(editedPayload: SafetyGatePayload? = null) {
        val payload = editedPayload ?: _uiState.value.activeSafetyGatePayload ?: return
        viewModelScope.launch {
            if (payload.intent == IntentType.QUERY_BUSINESS_DATA) {
                _uiState.update { it.copy(activeSafetyGatePayload = null, selectedScreen = AppScreen.AI_ANALYTICS) }
                repository.executeBusinessAnalyticsQuery(payload.rawTranscript)
            } else {
                val resultMsg = repository.executeSafetyGateMutation(payload)
                _uiState.update {
                    it.copy(
                        activeSafetyGatePayload = null,
                        userFeedbackMessage = resultMsg
                    )
                }
            }
        }
    }

    fun sendAiChatQuery(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            repository.executeBusinessAnalyticsQuery(query)
        }
    }

    fun recordManualCustomerEntry(customerId: String, customerName: String, amount: Double, isGaveCredit: Boolean, note: String) {
        viewModelScope.launch {
            repository.recordManualCustomerTransaction(customerId, customerName, amount, isGaveCredit, note)
            _uiState.update {
                it.copy(userFeedbackMessage = if (isGaveCredit) "Recorded ₹$amount Udhaar (Gave)" else "Recorded ₹$amount Payment (Got)")
            }
        }
    }

    fun addNewCustomer(name: String, phone: String, initialBalance: Double, notes: String) {
        viewModelScope.launch {
            repository.addNewCustomer(name, phone, initialBalance, notes)
            _uiState.update { it.copy(userFeedbackMessage = "Customer $name added successfully!") }
        }
    }

    fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            repository.deleteCustomer(customerId)
            _uiState.update {
                it.copy(
                    userFeedbackMessage = "Customer removed",
                    selectedScreen = if (it.selectedScreen == AppScreen.CUSTOMER_DETAIL) AppScreen.CUSTOMERS else it.selectedScreen
                )
            }
        }
    }

    fun addExpense(category: String, amount: Double, note: String, paymentMode: String) {
        viewModelScope.launch {
            repository.addExpense(category, amount, note, paymentMode)
            _uiState.update { it.copy(userFeedbackMessage = "Recorded ₹$amount for $category") }
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            repository.deleteExpense(id)
            _uiState.update { it.copy(userFeedbackMessage = "Expense deleted") }
        }
    }

    fun updateReminderStatus(id: String, status: String) {
        viewModelScope.launch {
            repository.updateReminderStatus(id, status)
            _uiState.update { it.copy(userFeedbackMessage = "Reminder marked as $status") }
        }
    }

    fun deleteReminder(id: String) {
        viewModelScope.launch {
            repository.deleteReminder(id)
            _uiState.update { it.copy(userFeedbackMessage = "Reminder deleted") }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearAiChat()
        }
    }
}
