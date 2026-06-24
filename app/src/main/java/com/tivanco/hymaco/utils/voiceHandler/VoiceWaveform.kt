package com.tivanco.hymaco.utils.voiceHandler

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

@Composable
fun VoiceWaveform(
    amplitudes: List<Float>,
    durationSeconds: Int,
    progressSeconds: Float,
    modifier: Modifier = Modifier
) {
    if (amplitudes.isEmpty()) return

    val safeDuration = if (durationSeconds > 0) durationSeconds else 1
    val highlightedIndex = ((progressSeconds / safeDuration) * amplitudes.size)
        .toInt()
        .coerceIn(0, amplitudes.size - 1)

    // رنگ‌های استایل واتس‌اپ
    val highlightColor = Color(0xFF412473) // رنگ پر شده
    val defaultColor = Color(0xFFB0B0B0)   // رنگ خاکستری خالی


    Canvas(modifier) {
        val totalBars = amplitudes.size
        // محاسبه فضای هر میله
        val slotWidth = size.width / totalBars
        // عرض خود میله (یه کم فاصله‌دارتر از قبل که قشنگ‌تر شه)
        val barWidth = slotWidth * 0.55f
        // گردی کامل لبه‌ها (واتس‌اپ لبه‌های کاملا گرد داره)
        val cornerRadius = CornerRadius(x = barWidth / 2, y = barWidth / 2)

        amplitudes.forEachIndexed { index, amp ->
            // واتس اپ یه مینیمم ارتفاعی همیشه داره که قشنگ باشه
            val safeAmp = amp.coerceIn(0.05f, 1f)
            val barHeight = safeAmp * size.height
            val yOffset = (size.height - barHeight) / 2f

            drawRoundRect(
                color = if (index <= highlightedIndex) highlightColor else defaultColor,
                topLeft = Offset(
                    x = (index * slotWidth) + ((slotWidth - barWidth) / 2f),
                    y = yOffset
                ),
                size = Size(width = barWidth, height = barHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}