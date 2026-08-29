package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.BusinessEntity
import com.example.ui.theme.*
import com.example.ui.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceKadaiTopBar(
    business: BusinessEntity?,
    onOpenBlueprint: () -> Unit,
    onOpenAiChat: () -> Unit,
    onOpenSubscription: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectLanguage: (String) -> Unit = {}
) {
    val strings = LocalAppStrings.current
    var languageMenuExpanded by remember { mutableStateOf(false) }
    val currentLang = business?.language ?: "Tanglish"

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand and Store Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = EmeraldPrimary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "VoiceKadai Logo",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = strings.appName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val isPro = business?.planTier == "PRO" || business?.planTier == "BUSINESS"
                            Surface(
                                color = if (isPro) AmberContainer else EmeraldContainer,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { onOpenSubscription() }
                            ) {
                                Text(
                                    text = business?.planTier ?: "FREE",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (isPro) OnAmberContainer else EmeraldDark,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Text(
                            text = business?.name?.ifBlank { "My Kirana Store" } ?: "My Kirana Store",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Action Icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Language Switcher Dropdown
                    Box {
                        IconButton(
                            onClick = { languageMenuExpanded = true },
                            modifier = Modifier.testTag("top_bar_language_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Switch Language",
                                tint = EmeraldPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false }
                        ) {
                            val languages = listOf(
                                "Tanglish" to "Tanglish (Tamil + English)",
                                "தமிழ்" to "தமிழ் (Pure Tamil)",
                                "English" to "English",
                                "हिंदी" to "हिंदी (Hindi)"
                            )
                            languages.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = label,
                                            fontWeight = if (currentLang.contains(code, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal,
                                            color = if (currentLang.contains(code, ignoreCase = true)) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        onSelectLanguage(label)
                                        languageMenuExpanded = false
                                    },
                                    leadingIcon = {
                                        if (currentLang.contains(code, ignoreCase = true)) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldPrimary)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Blueprint 12-Part System Architecture Document button
                    IconButton(
                        onClick = onOpenBlueprint,
                        modifier = Modifier.testTag("top_bar_blueprint_button")
                    ) {
                        Badge(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.White
                        ) {
                            Text("12", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(
                            imageVector = Icons.Outlined.MenuBook,
                            contentDescription = "System Architecture Blueprint (12 Parts)",
                            tint = EmeraldPrimary
                        )
                    }

                    // AI Chat Analytics button
                    IconButton(
                        onClick = onOpenAiChat,
                        modifier = Modifier.testTag("top_bar_ai_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "AI Business Analytics Chat",
                            tint = AmberSecondary
                        )
                    }

                    // Subscription Plans button
                    IconButton(
                        onClick = onOpenSubscription,
                        modifier = Modifier.testTag("top_bar_subscription_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Subscription Plans",
                            tint = AmberSecondary
                        )
                    }

                    // Settings button
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("top_bar_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Store Settings",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
