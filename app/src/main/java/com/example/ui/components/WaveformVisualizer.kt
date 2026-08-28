package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary

@Composable
fun WaveformVisualizer(
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 18,
    primaryColor: Color = EmeraldPrimary,
    accentColor: Color = AmberSecondary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalWidth = size.width
            val totalHeight = size.height
            val barWidth = (totalWidth / (barCount * 1.8f)).coerceIn(4f, 16f)
            val spacing = (totalWidth - (barCount * barWidth)) / (barCount - 1)

            val brush = Brush.verticalGradient(
                colors = listOf(primaryColor, accentColor)
            )

            for (i in 0 until barCount) {
                val x = i * (barWidth + spacing)
                val baseFactor = if (isRecording) {
                    val sinVal = kotlin.math.sin(Math.toRadians((phase + i * 22.0)).toFloat())
                    0.25f + 0.70f * kotlin.math.abs(sinVal)
                } else {
                    0.12f
                }

                val barHeight = (totalHeight * baseFactor).coerceAtLeast(6f)
                val y = (totalHeight - barHeight) / 2f

                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                )
            }
        }
    }
}
