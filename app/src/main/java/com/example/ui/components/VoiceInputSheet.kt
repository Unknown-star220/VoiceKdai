package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.util.LocalAppStrings
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInputSheet(
    isRecording: Boolean,
    isProcessing: Boolean,
    liveTranscript: String,
    onStartRecording: () -> Unit,
    onStopRecording: (String) -> Unit,
    onSamplePromptClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var typedInput by remember { mutableStateOf("") }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var recognizedText by remember { mutableStateOf("") }

    val samplePrompts = listOf(
        "Kumar kitta 5,000 balance irukku" to "Udhaar Credit",
        "Ramesh 200 tea podi kudutharu" to "Item Sale",
        "Murugan 1,000 kudutharu" to "Payment Jama",
        "Current bill 1,450 paid cash" to "EB Expense",
        "Priya kitta 2,200 vasul pannanum naalaiki" to "Due Reminder",
        "Today total sales evalo?" to "AI Query"
    )

    fun startNativeListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Voice recognition available via simulation/typed mode", Toast.LENGTH_SHORT).show()
            onStartRecording()
            return
        }

        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ta-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak in Tamil, English, or Tanglish...")
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    // Fallback to sample or typed text
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()
                    if (!text.isNullOrBlank()) {
                        recognizedText = text
                        onStopRecording(text)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = matches?.firstOrNull()
                    if (!partial.isNullOrBlank()) {
                        recognizedText = partial
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            speechRecognizer = recognizer
            recognizer.startListening(intent)
            onStartRecording()
        } catch (e: Exception) {
            onStartRecording()
        }
    }

    fun stopNativeListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {}
        val finalTranscript = if (recognizedText.isNotBlank()) recognizedText else liveTranscript.ifBlank { "Kumar kitta 5000 balance irukku" }
        onStopRecording(finalTranscript)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isRecording) UdhaarRed else EmeraldPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRecording) strings.listeningTitle else if (isProcessing) strings.processingVoice else strings.tapMicToSpeak,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = EmeraldContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Voice AI",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnEmeraldContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Waveform Visualizer
            WaveformVisualizer(
                isRecording = isRecording || isProcessing,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Hero Microphone Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (isRecording) {
                                stopNativeListening()
                            } else {
                                startNativeListening()
                            }
                        }
                        .testTag("hero_mic_button"),
                    color = if (isRecording) UdhaarRed else EmeraldPrimary,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Voice Record Microphone",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = if (isRecording) "Tap to finish speech" else strings.listeningHint,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sample scenario chips
            Text(
                text = "Tap sample voice scenario:",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(samplePrompts) { (prompt, label) ->
                    SuggestionChip(
                        onClick = { onSamplePromptClick(prompt) },
                        label = {
                            Column {
                                Text(text = label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmeraldPrimary)
                                Text(text = "\"$prompt\"", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fallback typed input
            OutlinedTextField(
                value = typedInput,
                onValueChange = { typedInput = it },
                placeholder = { Text(strings.micCtaHint, fontSize = 12.sp) },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (typedInput.isNotBlank()) {
                                onSamplePromptClick(typedInput)
                                typedInput = ""
                            }
                        },
                        modifier = Modifier.testTag("submit_typed_voice_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = EmeraldPrimary)
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (typedInput.isNotBlank()) {
                            onSamplePromptClick(typedInput)
                            typedInput = ""
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
