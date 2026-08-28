package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CustomerEntity
import com.example.data.local.entities.TransactionEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customer: CustomerEntity?,
    transactions: List<TransactionEntity>,
    onBackClick: () -> Unit,
    onRecordTransaction: (customerId: String, customerName: String, amount: Double, isGaveCredit: Boolean, note: String) -> Unit,
    onDeleteCustomer: (String) -> Unit
) {
    val context = LocalContext.current
    var showEntryDialog by remember { mutableStateOf<Boolean?>(null) } // true = Gave ₹ (Credit), false = Got ₹ (Jama), null = closed
    var entryAmount by remember { mutableStateOf("") }
    var entryNote by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    if (customer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Customer not found")
        }
        return
    }

    val balance = customer.currentBalance
    val isUdhaar = balance > 0
    val isAdvance = balance < 0

    val (badgeColor, balanceTitle, balanceSub) = when {
        isUdhaar -> Triple(UdhaarRed, "₹${String.format("%,.0f", balance)}", "You will get (கடன் வசூல் பாக்கி)")
        isAdvance -> Triple(JamaGreen, "₹${String.format("%,.0f", Math.abs(balance))}", "You will give (முன்பணம்)")
        else -> Triple(EmeraldPrimary, "₹0", "Settled (கணக்கு நேர் செய்யப்பட்டது)")
    }

    fun shareWhatsAppReminder() {
        val message = "Vanakkam ${customer.name}, this is a gentle reminder from Sri Lakshmi Kirana Store regarding your pending ledger balance of ₹${String.format("%,.0f", balance)}. Kindly settle via GPay/PhonePe or Cash at your earliest convenience. Nandri! 🙏"
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                if (customer.phone.isNotBlank()) {
                    putExtra("jid", "${customer.phone.replace("+", "").replace(" ", "")}@s.whatsapp.net")
                }
            }
            context.startActivity(Intent.createChooser(intent, "Share WhatsApp Reminder"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open share intent", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(customer.name, fontWeight = FontWeight.Bold, maxLines = 1)
                        if (customer.phone.isNotBlank()) {
                            Text(customer.phone, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { shareWhatsAppReminder() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Reminder", tint = EmeraldPrimary)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete Customer", tint = TextMuted)
                    }
                }
            )
        },
        bottomBar = {
            // Gave ₹ (Credit) and Got ₹ (Jama) Buttons
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showEntryDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("gave_credit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = UdhaarRed),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gave ₹ (Udhaar)", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showEntryDialog = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("got_payment_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = JamaGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Got ₹ (Jama)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingVals ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .testTag("customer_detail_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Net Balance Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = balanceSub,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = balanceTitle,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (isUdhaar) {
                            Button(
                                onClick = { shareWhatsAppReminder() },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send WhatsApp Reminder (வாட்ஸ்அப் நினைவு)")
                            }
                        }
                    }
                }
            }

            // Customer Transactions Ledger Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ledger History (கணக்கு பட்டியல்)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${transactions.size} Entries",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            // Customer Transactions
            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transaction history for this customer yet.", color = TextSecondary)
                    }
                }
            } else {
                items(transactions) { tx ->
                    val isGave = tx.type == "SALE_CREDIT"
                    val (tagColor, tagBg, symbol, label) = if (isGave) {
                        listOf(UdhaarRed, UdhaarRedContainer, "+₹", "GAVE (Udhaar)")
                    } else {
                        listOf(JamaGreen, JamaGreenContainer, "-₹", "GOT (Jama)")
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = tagBg as Color,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = label as String,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = tagColor as Color,
                                            fontSize = 9.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = dateFormat.format(Date(tx.createdAt)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tx.note.ifBlank { "Khata transaction" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!tx.audioTranscript.isNullOrBlank()) {
                                    Text(
                                        text = "Voice: \"${tx.audioTranscript}\"",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Text(
                                text = "$symbol${String.format("%,.0f", tx.amount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = tagColor as Color
                            )
                        }
                    }
                }
            }
        }
    }

    // Manual Entry Dialog
    if (showEntryDialog != null) {
        val isGave = showEntryDialog == true
        AlertDialog(
            onDismissRequest = { showEntryDialog = null },
            title = {
                Text(
                    text = if (isGave) "Gave ₹ (Udhaar) to ${customer.name}" else "Got ₹ (Payment) from ${customer.name}",
                    fontWeight = FontWeight.Bold,
                    color = if (isGave) UdhaarRed else JamaGreen
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = entryAmount,
                        onValueChange = { entryAmount = it },
                        label = { Text("Amount (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = entryNote,
                        onValueChange = { entryNote = it },
                        label = { Text("Bill / Item Details") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = entryAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onRecordTransaction(customer.id, customer.name, amt, isGave, entryNote)
                            entryAmount = ""
                            entryNote = ""
                            showEntryDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isGave) UdhaarRed else JamaGreen)
                ) {
                    Text("Save Entry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEntryDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirm Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Customer?") },
            text = { Text("Are you sure you want to remove ${customer.name} from your customer directory?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCustomer(customer.id)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UdhaarRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
