package com.tivanco.hymaco.customViews

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class LedState {
    OFF,      // خاموش
    OPENING,  // در حال باز شدن
    OPEN,     // باز
    CLOSING,  // در حال بسته شدن
    CLOSED    // بسته
}

@Composable
fun LedIndicator(
    state: LedState,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    // انیمیشن چشمک زدن برای حالت‌های در حال انجام
    val infiniteTransition = rememberInfiniteTransition(label = "led_blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )

    val (color, label, shouldBlink) = when (state) {
        LedState.OFF -> Triple(Color(0xFF444444), "خاموش", false)
        LedState.OPENING -> Triple(Color(0xFFFFA500), "در حال باز شدن", true)
        LedState.OPEN -> Triple(Color(0xFF00FF00), "باز", false)
        LedState.CLOSING -> Triple(Color(0xFFFFA500), "در حال بسته شدن", true)
        LedState.CLOSED -> Triple(Color(0xFFFF0000), "بسته", false)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LED خود
        Box(
            modifier = Modifier
                .size(16.dp)
                .shadow(
                    elevation = if (state != LedState.OFF) 8.dp else 0.dp,
                    shape = CircleShape,
                    spotColor = color
                )
                .background(
                    color = color.copy(alpha = if (shouldBlink) alpha else 1f),
                    shape = CircleShape
                )
        )

        // برچسب
        if (showLabel) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
        }
    }
}