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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onSignInWithGoogle: (email: String, name: String, storeName: String, phone: String) -> Unit,
    onSignInWithMicrosoft: (email: String, name: String, storeName: String, phone: String) -> Unit,
    onSignInWithPhone: (phone: String, name: String, storeName: String) -> Unit,
    onExploreAsGuest: () -> Unit
) {
    var authMode by remember { mutableStateOf("SOCIAL") } // "SOCIAL", "PHONE", "CUSTOM_EMAIL"
    var phoneNumber by remember { mutableStateOf("+91 98401 23456") }
    var otpCode by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var storeName by remember { mutableStateOf("Sri Lakshmi Kirana & General Store") }
    var ownerName by remember { mutableStateOf("Merchant Owner") }
    var customEmail by remember { mutableStateOf("merchant.kirana@gmail.com") }
    var showCustomEmailField by remember { mutableStateOf(false) }

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
                // Top Brand Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    // Glowing Emerald Mic Badge
                    Surface(
                        color = EmeraldPrimary,
                        shape = CircleShape,
                        shadowElevation = 8.dp,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "VoiceKadai Logo",
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "VoiceKadai",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldDark
                    )

                    Text(
                        text = "Speak your business. We handle the records.",
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
                            text = "🇮🇳 Built for Indian MSME Merchants & Kirana Stores",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnAmberContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

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
                            text = "Sign In to Your Merchant Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sync your voice khata, ledgers & UPI reminders across devices",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 1. Developer Google Sign-In Button (PRO Plan)
                        Button(
                            onClick = {
                                onSignInWithGoogle(
                                    "safiya.umar13@gmail.com",
                                    "Safiya Umar",
                                    storeName,
                                    phoneNumber
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("btn_google_signin_dev"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1F1F1F)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    GoogleLogoIcon(modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Developer Login (Safiya Umar)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF1F1F1F)
                                        )
                                        Text(
                                            text = "safiya.umar13@gmail.com",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Surface(
                                    color = EmeraldContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "PRO PLAN",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldDark
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom Email / Other Google Account Button (FREE Plan)
                        if (!showCustomEmailField) {
                            OutlinedButton(
                                onClick = { showCustomEmailField = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_custom_email_toggle"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AlternateEmail, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign In with Other Email (Free Plan)", fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Other Merchant Email (Free Plan)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                        Surface(
                                            color = AmberContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "FREE PLAN (5/day)",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = OnAmberContainer
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = customEmail,
                                        onValueChange = { customEmail = it },
                                        label = { Text("Email Address") },
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = EmeraldPrimary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            val name = if (customEmail.contains("@")) customEmail.substringBefore("@").replace(".", " ").capitalize(Locale.ROOT) else "Merchant"
                                            onSignInWithGoogle(customEmail.trim(), name, storeName, phoneNumber)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                    ) {
                                        Text("Continue with this Email", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Microsoft Sign-In Button (FREE Plan)
                        Button(
                            onClick = {
                                onSignInWithMicrosoft(
                                    "merchant.kadai@outlook.com",
                                    "K. Ramanathan",
                                    storeName,
                                    phoneNumber
                                )
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
                                    text = "Continue with Microsoft (Free Plan)",
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

                        // 3. Mobile Number Input
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Mobile Number (WhatsApp/UPI)") },
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
                                label = { Text("Enter 4-digit SMS OTP (Demo: 1234)") },
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
                                    otpSent = true
                                    otpCode = "1234"
                                } else {
                                    onSignInWithPhone(phoneNumber, ownerName, storeName)
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
                            Text(if (otpSent) "Verify OTP & Enter Kadai" else "Send Login OTP", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Guest / Demo Explore Button
                TextButton(
                    onClick = onExploreAsGuest,
                    modifier = Modifier.testTag("btn_guest_explore")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Explore Sri Lakshmi Kirana Demo Store", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Privacy and Trust Note
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

        // Draw clean multi-colored Google 'G' geometry
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
