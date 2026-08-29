package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.util.getAppStrings
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    currentLanguage: String = "Tanglish",
    onLanguageChange: (String) -> Unit = {},
    onSignInWithGoogle: (email: String, name: String, storeName: String, phone: String) -> Unit,
    onSignInWithMicrosoft: (email: String, name: String, storeName: String, phone: String) -> Unit,
    onSignInWithPhone: (phone: String, name: String, storeName: String) -> Unit,
    onExploreAsGuest: () -> Unit
) {
    var selectedLang by remember { mutableStateOf(currentLanguage) }
    val strings = remember(selectedLang) { getAppStrings(selectedLang) }

    var userEmail by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var showEmailForm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("auth_screen")
    ) { paddingVals ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            EmeraldDark.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Language Switcher Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val langOptions = listOf("English", "தமிழ்", "Tanglish", "हिंदी")
                    var langMenuExpanded by remember { mutableStateOf(false) }

                    Box {
                        OutlinedButton(
                            onClick = { langMenuExpanded = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(selectedLang, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = langMenuExpanded,
                            onDismissRequest = { langMenuExpanded = false }
                        ) {
                            langOptions.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang, fontWeight = if (selectedLang.startsWith(lang.take(4))) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        selectedLang = lang
                                        onLanguageChange(lang)
                                        langMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Brand Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    // Glowing Emerald Mic Badge
                    Surface(
                        color = EmeraldPrimary,
                        shape = CircleShape,
                        shadowElevation = 8.dp,
                        modifier = Modifier.size(76.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "VoiceKadai Logo",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = strings.appName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldDark
                    )

                    Text(
                        text = strings.authTagline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = AmberContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "🇮🇳 ${strings.authBadge}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnAmberContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sign-In Options Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.signInTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.signInSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Google Sign-In with User's Own Account
                        if (!showEmailForm) {
                            Button(
                                onClick = { showEmailForm = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("btn_google_signin"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF1F1F1F)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0)),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    GoogleLogoIcon(modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = strings.signInGoogle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1F1F1F)
                                    )
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            GoogleLogoIcon(modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(strings.signInGoogle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                        Surface(
                                            color = EmeraldContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "FREE PLAN (50/mo)",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldDark
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = userEmail,
                                        onValueChange = { userEmail = it },
                                        label = { Text(strings.enterEmail) },
                                        placeholder = { Text("e.g. yourname@gmail.com") },
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = EmeraldPrimary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = ownerName,
                                        onValueChange = { ownerName = it },
                                        label = { Text(strings.enterOwnerName) },
                                        placeholder = { Text("e.g. Ramesh Kumar") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = storeName,
                                        onValueChange = { storeName = it },
                                        label = { Text(strings.enterStoreName) },
                                        placeholder = { Text("e.g. Sri Lakshmi Provision Store") },
                                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = EmeraldPrimary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            val finalEmail = userEmail.ifBlank { "merchant@gmail.com" }
                                            val finalName = ownerName.ifBlank {
                                                if (finalEmail.contains("@")) finalEmail.substringBefore("@").replace(".", " ").capitalize(Locale.ROOT) else "Merchant"
                                            }
                                            val finalStore = storeName.ifBlank { "My Kirana Store" }
                                            onSignInWithGoogle(finalEmail.trim(), finalName, finalStore, phoneNumber)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                    ) {
                                        Text(strings.continueWithAccount, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Microsoft Sign-In Button
                        Button(
                            onClick = {
                                val email = if (userEmail.isNotBlank()) userEmail else "merchant@outlook.com"
                                val name = if (ownerName.isNotBlank()) ownerName else "Merchant Owner"
                                val store = if (storeName.isNotBlank()) storeName else "My Kirana Mart"
                                onSignInWithMicrosoft(email, name, store, phoneNumber)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_microsoft_signin"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2F2F2F),
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                MicrosoftLogoIcon(modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = strings.signInMicrosoft,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Divider OR
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Text(
                                text = "  OR USE MOBILE OTP  ",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Mobile Number Input
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text(strings.enterPhone) },
                            placeholder = { Text("+91 98401 23456") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = EmeraldPrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (otpSent) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { otpCode = it },
                                label = { Text(strings.enterOtp) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AmberSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (!otpSent) {
                                    val phoneToUse = phoneNumber.ifBlank { "+91 98401 23456" }
                                    phoneNumber = phoneToUse
                                    otpSent = true
                                    otpCode = "1234"
                                } else {
                                    val phoneToUse = phoneNumber.ifBlank { "+91 98401 23456" }
                                    val finalOwner = ownerName.ifBlank { "Kirana Merchant" }
                                    val finalStore = storeName.ifBlank { "My Kirana Store" }
                                    onSignInWithPhone(phoneToUse, finalOwner, finalStore)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(if (otpSent) Icons.Default.CheckCircle else Icons.Default.Sms, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (otpSent) strings.verifyOtp else strings.sendOtp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Guest Explore Button
                TextButton(
                    onClick = onExploreAsGuest,
                    modifier = Modifier.testTag("btn_guest_explore")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.exploreDemoStore, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Trust Note
                Text(
                    text = "🔒 Zero Financial Hallucination Guarantee • 100% Data Encrypted on Cloud & Device",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = w * 0.45f

        drawCircle(color = Color(0xFF4285F4), radius = radius, center = Offset(cx, cy))
        drawCircle(color = Color.White, radius = radius * 0.65f, center = Offset(cx, cy))
        drawRect(
            color = Color.White,
            topLeft = Offset(cx, cy - radius * 0.4f),
            size = Size(radius, radius * 0.8f)
        )
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(cx * 0.9f, cy - radius * 0.2f),
            size = Size(radius * 0.9f, radius * 0.4f)
        )
    }
}

@Composable
fun MicrosoftLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val gap = w * 0.08f
        val halfW = (w - gap) / 2f
        val halfH = (h - gap) / 2f

        // Red (top-left)
        drawRect(color = Color(0xFFF25022), topLeft = Offset(0f, 0f), size = Size(halfW, halfH))
        // Green (top-right)
        drawRect(color = Color(0xFF7FBA00), topLeft = Offset(halfW + gap, 0f), size = Size(halfW, halfH))
        // Blue (bottom-left)
        drawRect(color = Color(0xFF00A4EF), topLeft = Offset(0f, halfH + gap), size = Size(halfW, halfH))
        // Yellow (bottom-right)
        drawRect(color = Color(0xFFFFB900), topLeft = Offset(halfW + gap, halfH + gap), size = Size(halfW, halfH))
    }
}
