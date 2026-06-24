package com.tivanco.hymaco.utils

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun BlinkingErrorText(text: String, isError: Boolean) {
    // یه متغیر که وضعیت چشمک زدن رو کنترل می‌کنه
    var isBlinkOn by remember { mutableStateOf(false) }

    // وقتی خطا وجود داره، هر ۵۰۰ میلی‌ثانیه لامپ رو خاموش و روشن می‌کنیم
    LaunchedEffect(isError) {
        if (isError) {
            while (true) {
                isBlinkOn = !isBlinkOn
                delay(500) // سرعت چشمک زدن
            }
        } else {
            // اگه خطایی نبود، لامپ کاملاً خاموش بمونه
            isBlinkOn = false
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // اینجا فقط وضعیت رو می‌فرستیم، خود لامپ رنگ و نورش رو انیمیت می‌کنه
        LedIndicator(isOn = isBlinkOn, onColor = Color.Red)
        Text(text = text, fontSize = 16.sp, color = Color.Black)
    }
}
@Composable
fun LedIndicator(
    modifier: Modifier = Modifier,
    isOn: Boolean,
    size: Dp = responsiveSizes().ledSize,
    onColor: Color = Color.Green,
    offColor: Color = Color.Gray
) {
    // انیمیشن نرم برای تغییر رنگ خود لامپ
    val color by animateColorAsState(
        targetValue = if (isOn) onColor else offColor,
        animationSpec = tween(500),
        label = "ledColor"
    )

    // انیمیشن نرم برای شفافیت (آلفا) هاله نور
    // وقتی خاموش میشه، هاله نور کاملاً محو (0f) میشه
    val glowAlpha by animateFloatAsState(
        targetValue = if (isOn) 0.7f else 0f,
        animationSpec = tween(500),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            // پدینگ رو ثابت دادیم (8.dp) تا موقع چشمک زدن، سایز Box عوض نشه و متن کنارش نلرزه!
            .padding(8.dp)
            .size(size)
            .drawBehind {
                // فقط در صورتی که هاله نور شفافیت داره، رسمش کن
                if (glowAlpha > 0f) {
                    val radius = size.toPx() / 2f
                    val glowRadius = radius * 3f

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                // رنگ هاله با آلفای داینامیک که نرم محو میشه
                                color.copy(alpha = glowAlpha),
                                Color.Transparent
                            ),
                            radius = glowRadius
                        ),
                        radius = glowRadius
                    )
                }
            }
            .clip(CircleShape)
            .background(color)
    )
}