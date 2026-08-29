package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.*
import com.example.ui.util.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    business: BusinessEntity?,
    onBackClick: () -> Unit,
    onNavigateToBlueprint: () -> Unit,
    onOpenSubscription: () -> Unit,
    onLanguageChange: (String) -> Unit = {},
    onSignOut: () -> Unit
) {
    val strings = LocalAppStrings.current
    var selectedLanguage by remember(business?.language) { mutableStateOf(business?.language ?: "Tanglish") }
    var safetyGateEnabled by remember { mutableStateOf(true) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsTitle, fontWeight = FontWeight.Bold) },
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
                .testTag("settings_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Signed-In User Account Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (business?.authProvider == "GOOGLE") Color(0xFF4285F4) else if (business?.authProvider == "MICROSOFT") Color(0xFF00A4EF) else EmeraldPrimary,
                                    shape = CircleShape,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (business?.authProvider == "GOOGLE") Icons.Default.AccountCircle else if (business?.authProvider == "MICROSOFT") Icons.Default.CorporateFare else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(business?.userDisplayName?.ifBlank { "Merchant Owner" } ?: "Merchant Owner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(business?.userEmail?.ifBlank { "merchant@gmail.com" } ?: "merchant@gmail.com", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Surface(
                                color = EmeraldContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = business?.authProvider ?: "GOOGLE",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            }
                        }
                    }
                }
            }

            // Business Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = EmeraldPrimary,
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Store, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(business?.name?.ifBlank { "My Kirana Store" } ?: "My Kirana Store", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Owner: ${business?.ownerName?.ifBlank { "Merchant Owner" } ?: "Merchant Owner"} • ${business?.phone ?: "+91 98401 23456"}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Category:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(business?.category ?: "Kirana & Provision Store", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Active Plan:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            val isPro = business?.planTier == "PRO" || business?.planTier == "BUSINESS"
                            Text(
                                text = "${business?.planTier ?: "FREE"} Plan (${business?.maxDailyVoiceQuota ?: 50}/mo)",
                                fontWeight = FontWeight.Bold,
                                color = if (isPro) AmberSecondary else EmeraldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onOpenSubscription,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Subscription Plans (Free / Pro ₹299 / Business ₹999)")
                        }
                    }
                }
            }

            // Language Preference Card (Full App Language Switching)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.languageSelection, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        val languages = listOf(
                            "English" to "English (Global)",
                            "தமிழ்" to "தமிழ் (Pure Tamil)",
                            "Tanglish" to "Tanglish (Tamil + English)",
                            "हिंदी" to "हिंदी (Hindi)"
                        )
                        languages.forEach { (code, label) ->
                            val isSelected = selectedLanguage.startsWith(code.take(4))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) EmeraldContainer.copy(alpha = 0.4f) else Color.Transparent)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) EmeraldDark else MaterialTheme.colorScheme.onSurface
                                )
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        selectedLanguage = code
                                        onLanguageChange(code)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Zero Financial Hallucination Safeguard
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(strings.zeroHallucination, fontWeight = FontWeight.Bold)
                                    Text(strings.safetyGateDesc, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Switch(checked = safetyGateEnabled, onCheckedChange = { safetyGateEnabled = it })
                        }
                    }
                }
            }

            // Blueprint Explorer Action
            item {
                Button(
                    onClick = onNavigateToBlueprint,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.blueprint, fontWeight = FontWeight.Bold)
                }
            }

            // Sign Out / Switch Account Button
            item {
                OutlinedButton(
                    onClick = { showSignOutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_sign_out"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = UdhaarRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, UdhaarRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.signOut, fontWeight = FontWeight.Bold)
                }
            }

            // App Version Info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("VoiceKadai Mobile Edition v1.0.0", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text("Built for Indian Small Business Merchants • Zero Hallucination", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(strings.signOut) },
            text = { Text("You will return to the Sign-In screen. Your data remains safe on your device.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UdhaarRed)
                ) {
                    Text(strings.signOut)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
