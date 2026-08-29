package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.BusinessEntity
import com.example.data.local.entities.TransactionEntity
import com.example.ui.components.SummaryHeroCards
import com.example.ui.components.VoiceInputSheet
import com.example.ui.theme.*
import com.example.ui.util.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    business: BusinessEntity?,
    totalUdhaar: Double,
    todaySales: Double,
    todayExpenses: Double,
    recentTransactions: List<TransactionEntity>,
    isRecording: Boolean,
    isProcessingVoice: Boolean,
    liveTranscript: String,
    onStartRecording: () -> Unit,
    onStopRecording: (String) -> Unit,
    onSamplePromptClick: (String) -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToCustomerDetail: (String) -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToAiAnalytics: () -> Unit,
    onNavigateToBlueprint: () -> Unit
) {
    val strings = LocalAppStrings.current
    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Tagline Header Banner
        item {
            Surface(
                color = EmeraldDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "\"${strings.authTagline}\"",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldContainer
                        )
                        Text(
                            text = "🇮🇳 ${strings.authBadge} • ${strings.zeroHallucination}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = onNavigateToBlueprint,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("dashboard_view_blueprint_btn")
                    ) {
                        Text(strings.blueprint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Hero Summary Cards (Udhaar, Sales, Expenses)
        item {
            SummaryHeroCards(
                totalUdhaar = totalUdhaar,
                todaySales = todaySales,
                todayExpenses = todayExpenses,
                onUdhaarClick = onNavigateToCustomers,
                onSalesClick = onNavigateToAiAnalytics,
                onExpensesClick = onNavigateToExpenses
            )
        }

        // Voice Input & Push-to-Talk Mic Section
        item {
            VoiceInputSheet(
                isRecording = isRecording,
                isProcessing = isProcessingVoice,
                liveTranscript = liveTranscript,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording,
                onSamplePromptClick = onSamplePromptClick
            )
        }

        // Quick Merchant Actions Grid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = strings.quickActions,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.AddShoppingCart,
                        label = strings.gaveUdhaar,
                        color = EmeraldPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onSamplePromptClick("Ramesh 200 tea podi kudutharu") }
                    )
                    QuickActionButton(
                        icon = Icons.Default.CurrencyRupee,
                        label = strings.gotJama,
                        color = JamaGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onSamplePromptClick("Murugan 1000 kudutharu") }
                    )
                    QuickActionButton(
                        icon = Icons.Default.ReceiptLong,
                        label = strings.addExpense,
                        color = AmberSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToExpenses
                    )
                    QuickActionButton(
                        icon = Icons.Default.NotificationsActive,
                        label = strings.reminders,
                        color = UdhaarRed,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToReminders
                    )
                }
            }
        }

        // Recent Transaction Timeline Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.recentTransactions,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onNavigateToCustomers) {
                    Text(strings.allCustomers, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Recent Transactions List
        if (recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.noCustomersYet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(recentTransactions.take(6)) { tx ->
                val isCredit = tx.type == "SALE_CREDIT"
                val isPayment = tx.type == "PAYMENT_RECEIVED"

                val (badgeColor, badgeBg, prefix) = when {
                    isCredit -> Triple(UdhaarRed, UdhaarRedContainer, "${strings.gaveUdhaar}: +₹")
                    isPayment -> Triple(JamaGreen, JamaGreenContainer, "${strings.gotJama}: -₹")
                    else -> Triple(EmeraldPrimary, EmeraldContainer, "CASH: ₹")
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            if (tx.customerId != null) {
                                onNavigateToCustomerDetail(tx.customerId)
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                color = badgeBg,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isPayment) Icons.Default.ArrowDownward else if (isCredit) Icons.Default.ArrowUpward else Icons.Default.ShoppingBag,
                                        contentDescription = null,
                                        tint = badgeColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = tx.customerName ?: "Walk-in Cash Sale",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tx.note.ifBlank { tx.audioTranscript ?: "Transaction entry" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                                Text(
                                    text = dateFormat.format(Date(tx.createdAt)) + " • " + tx.paymentMode,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Text(
                            text = "$prefix${String.format("%,.0f", tx.amount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = color.copy(alpha = 0.12f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
