package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.PaymentCheckoutModal
import com.example.ui.components.SafetyGateModal
import com.example.ui.components.VoiceKadaiTopBar
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.VoiceKadaiViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: VoiceKadaiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceKadaiTheme {
                VoiceKadaiApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun VoiceKadaiApp(viewModel: VoiceKadaiViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val business by viewModel.business.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val totalUdhaar by viewModel.totalUdhaar.collectAsStateWithLifecycle()
    val totalAdvance by viewModel.totalAdvance.collectAsStateWithLifecycle()
    val todaySales by viewModel.todaySales.collectAsStateWithLifecycle()
    val todayExpenses by viewModel.todayExpenses.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userFeedbackMessage) {
        uiState.userFeedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissUserFeedback()
        }
    }

    val isSignedIn = business?.isSignedIn ?: true // defaults to true once initialized or if signed in

    if (!isSignedIn || uiState.selectedScreen == AppScreen.AUTH) {
        AuthScreen(
            onSignInWithGoogle = { email, name, storeName, phone ->
                viewModel.signInWithGoogle(email, name, storeName, phone)
            },
            onSignInWithMicrosoft = { email, name, storeName, phone ->
                viewModel.signInWithMicrosoft(email, name, storeName, phone)
            },
            onSignInWithPhone = { phone, name, storeName ->
                viewModel.signInWithPhone(phone, name, storeName)
            },
            onExploreAsGuest = {
                viewModel.signInAsGuest()
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("voicekadai_app_root"),
        topBar = {
            if (uiState.selectedScreen != AppScreen.CUSTOMER_DETAIL &&
                uiState.selectedScreen != AppScreen.BLUEPRINT_SPECS &&
                uiState.selectedScreen != AppScreen.SUBSCRIPTION &&
                uiState.selectedScreen != AppScreen.SETTINGS
            ) {
                VoiceKadaiTopBar(
                    business = business,
                    onOpenBlueprint = { viewModel.navigateTo(AppScreen.BLUEPRINT_SPECS) },
                    onOpenAiChat = { viewModel.navigateTo(AppScreen.AI_ANALYTICS) },
                    onOpenSubscription = { viewModel.navigateTo(AppScreen.SUBSCRIPTION) },
                    onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) }
                )
            }
        },
        bottomBar = {
            if (uiState.selectedScreen != AppScreen.CUSTOMER_DETAIL) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = uiState.selectedScreen == AppScreen.DASHBOARD,
                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.selectedScreen == AppScreen.DASHBOARD) Icons.Filled.Storefront else Icons.Outlined.Storefront,
                                contentDescription = "Dashboard"
                            )
                        },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedScreen == AppScreen.CUSTOMERS,
                        onClick = { viewModel.navigateTo(AppScreen.CUSTOMERS) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.selectedScreen == AppScreen.CUSTOMERS) Icons.Filled.PeopleAlt else Icons.Outlined.PeopleAlt,
                                contentDescription = "Khata Directory"
                            )
                        },
                        label = { Text("Khata", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_customers")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedScreen == AppScreen.EXPENSES,
                        onClick = { viewModel.navigateTo(AppScreen.EXPENSES) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.selectedScreen == AppScreen.EXPENSES) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                                contentDescription = "Expenses"
                            )
                        },
                        label = { Text("Expenses", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmberSecondary,
                            selectedTextColor = AmberSecondary,
                            indicatorColor = AmberContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_expenses")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedScreen == AppScreen.REMINDERS,
                        onClick = { viewModel.navigateTo(AppScreen.REMINDERS) },
                        icon = {
                            Badge(containerColor = UdhaarRed) {
                                Text("${reminders.count { it.status == "PENDING" }}", color = Color.White, fontSize = 9.sp)
                            }
                            Icon(
                                imageVector = if (uiState.selectedScreen == AppScreen.REMINDERS) Icons.Filled.NotificationsActive else Icons.Outlined.NotificationsActive,
                                contentDescription = "Reminders"
                            )
                        },
                        label = { Text("Remind", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UdhaarRed,
                            selectedTextColor = UdhaarRed,
                            indicatorColor = UdhaarRedContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_reminders")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedScreen == AppScreen.BLUEPRINT_SPECS,
                        onClick = { viewModel.navigateTo(AppScreen.BLUEPRINT_SPECS) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.selectedScreen == AppScreen.BLUEPRINT_SPECS) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                                contentDescription = "12-Part Spec"
                            )
                        },
                        label = { Text("Blueprint", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_blueprint")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.selectedScreen) {
                AppScreen.AUTH -> {
                    // Handled above
                }

                AppScreen.DASHBOARD -> DashboardScreen(
                    business = business,
                    totalUdhaar = totalUdhaar,
                    todaySales = todaySales,
                    todayExpenses = todayExpenses,
                    recentTransactions = transactions,
                    isRecording = uiState.isRecording,
                    isProcessingVoice = uiState.isProcessingVoice,
                    liveTranscript = uiState.liveTranscript,
                    onStartRecording = { viewModel.startVoiceRecording() },
                    onStopRecording = { text -> viewModel.stopAndProcessVoiceRecording(text) },
                    onSamplePromptClick = { text -> viewModel.triggerSampleVoicePrompt(text) },
                    onNavigateToCustomers = { viewModel.navigateTo(AppScreen.CUSTOMERS) },
                    onNavigateToCustomerDetail = { custId -> viewModel.navigateTo(AppScreen.CUSTOMER_DETAIL, custId) },
                    onNavigateToExpenses = { viewModel.navigateTo(AppScreen.EXPENSES) },
                    onNavigateToReminders = { viewModel.navigateTo(AppScreen.REMINDERS) },
                    onNavigateToAiAnalytics = { viewModel.navigateTo(AppScreen.AI_ANALYTICS) },
                    onNavigateToBlueprint = { viewModel.navigateTo(AppScreen.BLUEPRINT_SPECS) }
                )

                AppScreen.CUSTOMERS -> CustomersScreen(
                    customers = customers,
                    totalUdhaar = totalUdhaar,
                    totalAdvance = totalAdvance,
                    searchQuery = uiState.searchQuery,
                    filter = uiState.customerFilter,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onFilterChange = { viewModel.setCustomerFilter(it) },
                    onCustomerClick = { custId -> viewModel.navigateTo(AppScreen.CUSTOMER_DETAIL, custId) },
                    onAddNewCustomer = { name, phone, balance, notes -> viewModel.addNewCustomer(name, phone, balance, notes) },
                    onVoiceEntryPrompt = { prompt -> viewModel.triggerSampleVoicePrompt(prompt) }
                )

                AppScreen.CUSTOMER_DETAIL -> {
                    val customer = customers.find { it.id == uiState.selectedCustomerId }
                    val customerTxs = transactions.filter { it.customerId == uiState.selectedCustomerId }
                    CustomerDetailScreen(
                        customer = customer,
                        transactions = customerTxs,
                        onBackClick = { viewModel.navigateTo(AppScreen.CUSTOMERS) },
                        onRecordTransaction = { custId, name, amt, isGave, note ->
                            viewModel.recordManualCustomerEntry(custId, name, amt, isGave, note)
                        },
                        onDeleteCustomer = { custId -> viewModel.deleteCustomer(custId) }
                    )
                }

                AppScreen.EXPENSES -> ExpensesScreen(
                    expenses = expenses,
                    todayExpenses = todayExpenses,
                    onAddExpense = { cat, amt, note, mode -> viewModel.addExpense(cat, amt, note, mode) },
                    onDeleteExpense = { id -> viewModel.deleteExpense(id) }
                )

                AppScreen.REMINDERS -> RemindersScreen(
                    reminders = reminders,
                    onUpdateStatus = { id, status -> viewModel.updateReminderStatus(id, status) },
                    onDeleteReminder = { id -> viewModel.deleteReminder(id) },
                    onVoiceEntryPrompt = { prompt -> viewModel.triggerSampleVoicePrompt(prompt) }
                )

                AppScreen.AI_ANALYTICS -> AiChatAnalyticsScreen(
                    messages = aiMessages,
                    onSendMessage = { query -> viewModel.sendAiChatQuery(query) },
                    onClearChat = { viewModel.clearChat() }
                )

                AppScreen.BLUEPRINT_SPECS -> SystemBlueprintScreen(
                    activePart = uiState.activeBlueprintPart,
                    onSelectPart = { part -> viewModel.setBlueprintPart(part) },
                    onBackClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                )

                AppScreen.SUBSCRIPTION -> SubscriptionScreen(
                    business = business,
                    paymentHistory = payments,
                    onBackClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    onOpenCheckout = { tier, cycle -> viewModel.openPaymentCheckout(tier, cycle) }
                )

                AppScreen.SETTINGS -> SettingsScreen(
                    business = business,
                    onBackClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    onNavigateToBlueprint = { viewModel.navigateTo(AppScreen.BLUEPRINT_SPECS) },
                    onOpenSubscription = { viewModel.navigateTo(AppScreen.SUBSCRIPTION) },
                    onSignOut = { viewModel.signOut() }
                )
            }

            // Zero Financial Hallucination Action Gate Modal
            uiState.activeSafetyGatePayload?.let { payload ->
                SafetyGateModal(
                    payload = payload,
                    onDismiss = { viewModel.dismissSafetyGate() },
                    onConfirm = { editedPayload -> viewModel.confirmSafetyGateMutation(editedPayload) }
                )
            }

            // Real Money In-App Subscription Payment Checkout Modal
            uiState.activeCheckoutPlan?.let { plan ->
                PaymentCheckoutModal(
                    checkoutPlan = plan,
                    isProcessing = uiState.isProcessingPayment,
                    onDismiss = { viewModel.closePaymentCheckout() },
                    onConfirmPayment = { method, customRef ->
                        viewModel.processRealPayment(method, customRef)
                    }
                )
            }

            // Payment Celebration & Tax Invoice Dialog
            uiState.lastCompletedPayment?.let { completedPayment ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissPaymentSuccessDialog() },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = EmeraldContainer,
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Subscription Activated!", fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column {
                            Text("Congratulations! VoiceKadai ${completedPayment.planTier} Plan (${completedPayment.billingCycle}) is now active for your shop.", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Invoice No: ${completedPayment.invoiceNumber}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Text("Order ID: ${completedPayment.orderId}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text("Amount Paid: ₹${String.format("%.2f", completedPayment.totalPaid)} (incl. 18% GST)", fontWeight = FontWeight.SemiBold, color = EmeraldPrimary, style = MaterialTheme.typography.bodySmall)
                                    Text("Txn Ref: ${completedPayment.transactionRef}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.dismissPaymentSuccessDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Continue to Kadai")
                        }
                    }
                )
            }
        }
    }
}
