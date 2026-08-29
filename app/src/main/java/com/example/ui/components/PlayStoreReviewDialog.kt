package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.ui.util.LocalAppStrings

@Composable
fun PlayStoreReviewDialog(
    onDismiss: () -> Unit,
    onRateOnPlayStore: (stars: Int) -> Unit,
    onSubmitFeedback: (stars: Int, comments: String) -> Unit,
    onRemindLater: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current

    var selectedStars by remember { mutableIntStateOf(5) }
    var feedbackText by remember { mutableStateOf("") }
    val selectedFeatureChips = remember { mutableStateListOf("Fast Voice Khata", "Zero Hallucination") }

    val starAnimationScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "star_scale"
    )

    fun openPlayStore(stars: Int) {
        onRateOnPlayStore(stars)
        val packageName = context.packageName
        try {
            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(playStoreIntent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(webIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Google Play Store not available", Toast.LENGTH_SHORT).show()
            }
        }
        Toast.makeText(context, strings.feedbackSentToast, Toast.LENGTH_LONG).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 24.dp)
                .testTag("play_store_review_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 12.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Google Play Badge & Timer Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFF01875F).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFF01875F).copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shop,
                                contentDescription = null,
                                tint = Color(0xFF01875F),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Google Play",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF01875F)
                            )
                        }
                    }

                    Surface(
                        color = EmeraldContainer,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = EmeraldDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "1 Min Active",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // App Icon & Rating Header
                Surface(
                    color = EmeraldContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = EmeraldDark,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = strings.playStoreReviewTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = strings.playStoreReviewSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Interactive 5-Star Row
                Surface(
                    color = AmberContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, AmberSecondary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (i in 1..5) {
                                val isSelected = i <= selectedStars
                                IconButton(
                                    onClick = { selectedStars = i },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("star_$i")
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = "Star $i",
                                        tint = if (isSelected) Color(0xFFFFB300) else Color(0xFFBDBDBD),
                                        modifier = Modifier
                                            .size(36.dp)
                                            .scale(if (i == selectedStars) starAnimationScale else 1f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Dynamic Star Label
                        val ratingLabel = when (selectedStars) {
                            5 -> strings.playStoreRating5Title
                            4 -> strings.playStoreRating4Title
                            3 -> strings.playStoreRating3Title
                            else -> strings.playStoreRatingLowTitle
                        }

                        Text(
                            text = ratingLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedStars >= 4) EmeraldDark else AmberDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content branch: 4-5 Stars (Rate on Play Store) vs 1-3 Stars (Feedback Form)
                AnimatedContent(
                    targetState = selectedStars >= 4,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "review_mode"
                ) { isHighRating ->
                    if (isHighRating) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "What do you love most about VoiceKadai?",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Quick Feature Chips
                            val features = listOf(
                                "⚡ Fast Voice Khata",
                                "💰 Zero Hallucination",
                                "🗣️ Tamil/Hindi Audio",
                                "📲 WhatsApp Reminders"
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                features.take(2).forEach { feat ->
                                    val isPicked = selectedFeatureChips.contains(feat)
                                    FilterChip(
                                        selected = isPicked,
                                        onClick = {
                                            if (isPicked) selectedFeatureChips.remove(feat)
                                            else selectedFeatureChips.add(feat)
                                        },
                                        label = { Text(feat, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                features.drop(2).forEach { feat ->
                                    val isPicked = selectedFeatureChips.contains(feat)
                                    FilterChip(
                                        selected = isPicked,
                                        onClick = {
                                            if (isPicked) selectedFeatureChips.remove(feat)
                                            else selectedFeatureChips.add(feat)
                                        },
                                        label = { Text(feat, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Rate on Play Store Button
                            Button(
                                onClick = { openPlayStore(selectedStars) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("btn_rate_play_store"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.rateOnPlayStore, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = strings.feedbackPrompt,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = feedbackText,
                                onValueChange = { feedbackText = it },
                                placeholder = { Text(strings.feedbackPlaceholder, fontSize = 13.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .testTag("input_feedback_text"),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 4
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    onSubmitFeedback(selectedStars, feedbackText)
                                    Toast.makeText(context, strings.feedbackSentToast, Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("btn_submit_feedback"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.submitFeedback, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Actions: Remind Later & Not Now
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onRemindLater,
                        modifier = Modifier.testTag("btn_remind_later")
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.remindMeLater, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_dismiss_review")
                    ) {
                        Text(strings.notNow, color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
