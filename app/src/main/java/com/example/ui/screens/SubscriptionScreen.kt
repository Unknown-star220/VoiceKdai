package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.BusinessEntity
import com.example.data.local.entities.SubscriptionPaymentEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    business: BusinessEntity?,
    paymentHistory: List<SubscriptionPaymentEntity> = emptyList(),
    onBackClick: () -> Unit,
    onOpenCheckout: (planTier: String, billingCycle: String) -> Unit
) {
    val context = LocalContext.current
    var selectedTier by remember { mutableStateOf(business?.planTier ?: "PRO") }
    var isAnnual by remember { mutableStateOf(false) }
    var selectedInvoiceForView by remember { mutableStateOf<SubscriptionPaymentEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VoiceKadai Subscriptions & Quota", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingVals ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .testTag("subscription_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Usage Quota & Active Subscription Status Card
            item {
                val isDev = business?.userEmail?.trim()?.equals("safiya.umar13@gmail.com", ignoreCase = true) == true
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("TODAY'S VOICE USAGE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                Text("${business?.dailyVoiceCount ?: 0} / ${business?.maxDailyVoiceQuota ?: 5} Entries", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            }
                            Surface(
                                color = if (isDev) EmeraldPrimary else if (business?.planTier == "BUSINESS" || business?.planTier == "PRO") EmeraldPrimary else AmberContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isDev) "👑 DEVELOPER PRO" else "PLAN: ${business?.planTier ?: "FREE"}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (isDev || business?.planTier == "BUSINESS" || business?.planTier == "PRO") Color.White else OnAmberContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { ((business?.dailyVoiceCount ?: 0).toFloat() / (business?.maxDailyVoiceQuota ?: 5).toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = EmeraldPrimary,
                            trackColor = EmeraldContainer
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (isDev) {
                                Text("Status: Developer Lifetime Account", style = MaterialTheme.typography.bodySmall, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                            } else if (business?.planTier == "FREE") {
                                Text("Status: Free Plan (Subscribe for more)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            } else {
                                val expiryDateStr = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(business?.subscriptionExpiry ?: (System.currentTimeMillis() + 30L * 86400000)))
                                Text("Renews on: $expiryDateStr", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Text("${business?.userEmail?.ifBlank { "Merchant" }}", style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
                        }
                    }
                }
            }

            // Billing Cycle Selector (Monthly vs Annual)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isAnnual = false },
                            color = if (!isAnnual) EmeraldPrimary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Monthly Billing",
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = if (!isAnnual) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isAnnual = true },
                            color = if (isAnnual) EmeraldPrimary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Annual (Save 20%)",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAnnual) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // ROI Kirana Calculator Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = OnAmberContainer, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Merchant Financial Value", fontWeight = FontWeight.Bold, color = OnAmberContainer)
                            Text(
                                "VoiceKadai saves ~2.5 hours/day of register writing and recovers ₹8,000+ in forgotten customer udhaar every month.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Tiers List
            item {
                Text("Select In-App Subscription Plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            // 1. Free Tier
            item {
                PlanTierCard(
                    title = "Free (Kadaikaran)",
                    price = "₹0",
                    period = "forever",
                    description = "Default for regular merchant logins",
                    features = listOf("5 voice entries/day", "Basic Khata Udhaar & Jama", "Single device access"),
                    isSelected = selectedTier == "FREE",
                    onSelect = { selectedTier = "FREE" }
                )
            }

            // 2. Pro Tier (Recommended)
            item {
                PlanTierCard(
                    title = "Pro (Vyapar) — Recommended",
                    price = if (isAnnual) "₹2,499" else "₹299",
                    period = if (isAnnual) "/ year (₹208/mo)" else "/ month",
                    description = "For busy Kirana, hardware, and provision stores",
                    features = listOf(
                        "100 voice entries/day",
                        "Unlimited AI Business Analytics & Q&A",
                        "WhatsApp 1-Tap Reminders with UPI links",
                        "Zero Financial Hallucination Engine",
                        "Full Ledger CSV & PDF Export"
                    ),
                    isSelected = selectedTier == "PRO",
                    isPopular = true,
                    onSelect = { selectedTier = "PRO" }
                )
            }

            // 3. Business Tier
            item {
                PlanTierCard(
                    title = "Business (Super Kadai)",
                    price = if (isAnnual) "₹7,999" else "₹999",
                    period = if (isAnnual) "/ year (₹666/mo)" else "/ month",
                    description = "For high-volume retail shops with counter staff",
                    features = listOf(
                        "Unlimited voice entries",
                        "Multi-staff counter login with permissions",
                        "Automated Voice Call Reminders",
                        "Priority NLU processing queue",
                        "Dedicated WhatsApp Merchant Support"
                    ),
                    isSelected = selectedTier == "BUSINESS",
                    onSelect = { selectedTier = "BUSINESS" }
                )
            }

            // Real Money Upgrade Button
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        val cycle = if (isAnnual) "ANNUAL" else "MONTHLY"
                        onOpenCheckout(selectedTier, cycle)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_pay_subscription_real"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Pay Real Money: Subscribe to $selectedTier",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Supports Google Play Billing, UPI (GPay, PhonePe, Paytm), & Cards",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // Past Invoices and Payment History Section
            if (paymentHistory.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Payment History & Tax Invoices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(paymentHistory) { payment ->
                    InvoiceHistoryCard(
                        payment = payment,
                        onViewInvoice = { selectedInvoiceForView = payment }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Invoice Details Dialog
    selectedInvoiceForView?.let { invoice ->
        AlertDialog(
            onDismissRequest = { selectedInvoiceForView = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tax Invoice: ${invoice.invoiceNumber}")
                }
            },
            text = {
                Column {
                    Text("Merchant: ${business?.name ?: "Store"}", fontWeight = FontWeight.Bold)
                    Text("Order ID: ${invoice.orderId}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("Payment Method: ${invoice.paymentMethod}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("Txn Ref: ${invoice.transactionRef}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Plan: ${invoice.planTier} (${invoice.billingCycle})")
                        Text("₹${String.format("%.2f", invoice.amount)}")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("GST (18%):")
                        Text("₹${String.format("%.2f", invoice.taxAmount)}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount Paid:", fontWeight = FontWeight.Bold)
                        Text("₹${String.format("%.2f", invoice.totalPaid)}", fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Invoice ${invoice.invoiceNumber} downloaded to device", Toast.LENGTH_SHORT).show()
                        selectedInvoiceForView = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download PDF Invoice")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedInvoiceForView = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun PlanTierCard(
    title: String,
    price: String,
    period: String,
    description: String,
    features: List<String>,
    isSelected: Boolean,
    isPopular: Boolean = false,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(18.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) EmeraldContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (isPopular) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = EmeraldPrimary, shape = RoundedCornerShape(4.dp)) {
                                Text("POPULAR", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(price, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(period, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(bottom = 2.dp))
                    }
                }
                RadioButton(selected = isSelected, onClick = onSelect)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(10.dp))
            features.forEach { feat ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(feat, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun InvoiceHistoryCard(
    payment: SubscriptionPaymentEntity,
    onViewInvoice: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onViewInvoice() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(payment.invoiceNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(payment.createdAt))
                Text(dateStr, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("Method: ${payment.paymentMethod}", style = MaterialTheme.typography.bodySmall, color = EmeraldPrimary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₹${String.format("%.2f", payment.totalPaid)}", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = EmeraldDark)
                Surface(color = EmeraldContainer, shape = RoundedCornerShape(4.dp)) {
                    Text(payment.status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = EmeraldDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
