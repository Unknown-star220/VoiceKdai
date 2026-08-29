package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

data class PlanModel(
    val id: String,
    val emoji: String,
    val name: String,
    val monthlyPrice: String,
    val annualPrice: String,
    val billingCycleText: String,
    val subtitle: String,
    val tag: String?,
    val accentColor: Color,
    val features: List<String>,
    val icon: ImageVector,
    val isPopular: Boolean = false
)

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

    val plans = listOf(
        PlanModel(
            id = "FREE",
            emoji = "🆓",
            name = "Free Plan",
            monthlyPrice = "₹0",
            annualPrice = "₹0",
            billingCycleText = "Forever Free",
            subtitle = "Essential toolset for small shopkeepers and kirana stalls starting with voice entry",
            tag = "STARTER",
            accentColor = Color(0xFF64748B),
            features = listOf(
                "50 voice commands/month",
                "Basic customer records",
                "Basic dashboard"
            ),
            icon = Icons.Default.Mic,
            isPopular = false
        ),
        PlanModel(
            id = "PRO",
            emoji = "⭐",
            name = "Pro Plan",
            monthlyPrice = "₹299",
            annualPrice = "₹2,499",
            billingCycleText = "/month",
            subtitle = "For active retail merchants seeking full automation, smart reminders & growth",
            tag = "MOST POPULAR",
            accentColor = EmeraldPrimary,
            features = listOf(
                "Unlimited voice commands",
                "AI business reports",
                "Smart reminders",
                "Advanced analytics",
                "Multiple languages"
            ),
            icon = Icons.Default.Star,
            isPopular = true
        ),
        PlanModel(
            id = "BUSINESS",
            emoji = "🏢",
            name = "Business Plan",
            monthlyPrice = "₹999",
            annualPrice = "₹7,999",
            billingCycleText = "/month",
            subtitle = "For multi-counter stores, supermarkets & teams with multiple staff members",
            tag = "ENTERPRISE",
            accentColor = Color(0xFF6366F1),
            features = listOf(
                "Multiple employees",
                "Multiple devices",
                "Advanced reports",
                "Data backup",
                "Business analytics"
            ),
            icon = Icons.Default.BusinessCenter,
            isPopular = false
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Subscription Plans",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("subscription_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingVals ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .testTag("subscription_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Banner / Active Status
            item {
                ActiveSubscriptionStatusCard(
                    business = business,
                    voiceCommandCount = business?.dailyVoiceCount ?: 0,
                    maxFreeQuota = 50
                )
            }

            // Billing Cycle Toggle (Monthly vs Annual)
            item {
                BillingCycleToggleCard(
                    isAnnual = isAnnual,
                    onToggle = { isAnnual = it }
                )
            }

            // Plans List: Free, Pro, Business
            items(plans) { plan ->
                SubscriptionPlanCard(
                    plan = plan,
                    isAnnual = isAnnual,
                    isSelected = selectedTier.equals(plan.id, ignoreCase = true),
                    onSelect = { selectedTier = plan.id }
                )
            }

            // Action Upgrade / Subscribe Button
            item {
                val chosenPlan = plans.find { it.id.equals(selectedTier, ignoreCase = true) } ?: plans[1]
                val priceDisplay = if (selectedTier == "FREE") "Free" else if (isAnnual) chosenPlan.annualPrice else "${chosenPlan.monthlyPrice}/mo"

                Button(
                    onClick = {
                        val cycle = if (isAnnual) "ANNUAL" else "MONTHLY"
                        onOpenCheckout(selectedTier, cycle)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_subscribe_action"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTier == "BUSINESS") Color(0xFF4F46E5) else EmeraldPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Icon(
                        imageVector = if (selectedTier == "FREE") Icons.Default.CheckCircle else Icons.Default.WorkspacePremium,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (selectedTier == "FREE") "Current: Active Free Plan" else "Subscribe to ${chosenPlan.name} ($priceDisplay)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
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

            // Financial Potential & Revenue Scale Breakdown
            item {
                RevenuePotentialCard()
            }

            // Developer Account Direct Payout Card
            item {
                DeveloperPayoutCard(developerEmail = "safiya.umar13@gmail.com")
            }

            // Invoices & History (if available)
            if (paymentHistory.isNotEmpty()) {
                item {
                    Text(
                        "Payment History & Receipts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(paymentHistory) { payment ->
                    InvoiceHistoryCard(
                        payment = payment,
                        onViewInvoice = { selectedInvoiceForView = payment }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Invoice View Dialog
    selectedInvoiceForView?.let { invoice ->
        AlertDialog(
            onDismissRequest = { selectedInvoiceForView = null },
            icon = {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = EmeraldPrimary)
            },
            title = {
                Text("Tax Invoice: ${invoice.invoiceNumber}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Store: ${business?.name ?: "VoiceKadai Merchant"}", fontWeight = FontWeight.SemiBold)
                    Text("Order ID: ${invoice.orderId}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("Payment Method: ${invoice.paymentMethod}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Plan (${invoice.billingCycle}):")
                        Text("₹${String.format("%.2f", invoice.amount)}")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("GST (18%):")
                        Text("₹${String.format("%.2f", invoice.taxAmount)}")
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Paid:", fontWeight = FontWeight.Bold)
                        Text("₹${String.format("%.2f", invoice.totalPaid)}", fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Invoice ${invoice.invoiceNumber} saved", Toast.LENGTH_SHORT).show()
                        selectedInvoiceForView = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun ActiveSubscriptionStatusCard(
    business: BusinessEntity?,
    voiceCommandCount: Int,
    maxFreeQuota: Int
) {
    val activeTier = business?.planTier ?: "FREE"
    val isUnlimited = activeTier == "PRO" || activeTier == "BUSINESS"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_status_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "CURRENT SUBSCRIPTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    Text(
                        when (activeTier.uppercase()) {
                            "PRO" -> "⭐ Pro Plan (Active)"
                            "BUSINESS" -> "🏢 Business Plan (Active)"
                            else -> "🆓 Free Plan"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    color = if (isUnlimited) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (isUnlimited) "UNLIMITED" else "$voiceCommandCount/$maxFreeQuota CMDS",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlimited) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (!isUnlimited) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (voiceCommandCount.toFloat() / maxFreeQuota.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = EmeraldPrimary,
                    trackColor = EmeraldContainer
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Free tier allows 50 voice commands/month. Upgrade to Pro for unlimited AI voice processing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun BillingCycleToggleCard(
    isAnnual: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onToggle(false) },
                color = if (!isAnnual) EmeraldPrimary else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
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
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onToggle(true) },
                color = if (isAnnual) EmeraldPrimary else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Annual (Save 30%)",
                        fontWeight = FontWeight.Bold,
                        color = if (isAnnual) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionPlanCard(
    plan: PlanModel,
    isAnnual: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val price = if (plan.id == "FREE") {
        "₹0"
    } else if (isAnnual) {
        plan.annualPrice
    } else {
        plan.monthlyPrice
    }

    val period = if (plan.id == "FREE") {
        "forever"
    } else if (isAnnual) {
        "/ year"
    } else {
        "/ month"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) plan.accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("plan_card_${plan.id.lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) plan.accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        plan.emoji,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                plan.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (plan.isPopular) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = EmeraldPrimary,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "RECOMMENDED",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            plan.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    }
                }

                RadioButton(
                    selected = isSelected,
                    onClick = onSelect,
                    colors = RadioButtonDefaults.colors(selectedColor = plan.accentColor)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pricing Tag
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    price,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = plan.accentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    period,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )

            // Features Checklist
            Text(
                if (plan.id == "FREE") "Allow:" else "Include:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            plan.features.forEach { feat ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = plan.accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        feat,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (plan.id == "PRO" && feat.contains("Unlimited")) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun RevenuePotentialCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("revenue_potential_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Example Potential & Economics 📈",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "If you eventually get:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    Text(
                        "• 1,000 paying users × ₹299/month\n  = approximately ₹2.99 lakh/month gross recurring revenue",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "• At 10,000 paying users, the gross subscription revenue would be roughly ₹29.9 lakh/month.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                "Of course, you'll have AI, cloud, payment-processing, support, taxes, and marketing costs—but this shows why a useful business subscription can scale. 📈",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun DeveloperPayoutCard(developerEmail: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("developer_payout_card"),
        colors = CardDefaults.cardColors(
            containerColor = EmeraldContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.AccountBalance,
                contentDescription = null,
                tint = EmeraldDark,
                modifier = Modifier.size(28.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Google Play Store Direct Payout",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark
                )
                Text(
                    "When published to the Play Store, all In-App Subscription revenue is processed by Google Play Billing and deposited directly into the developer's registered merchant bank account ($developerEmail).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
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
