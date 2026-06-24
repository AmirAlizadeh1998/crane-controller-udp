package com.tivanco.hymaco.customViews

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tivanco.hymaco.R
import com.tivanco.hymaco.dataClass.Status
import kotlin.math.roundToInt

@Composable
fun RockerToggle(
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    status: Status,
    width: Dp = 60.dp,
    height: Dp = 34.dp,
    thumbPadding: Dp = 4.dp,
    onColor: Color = Color(0xFF4CAF50),
    offColor: Color = Color(0xFF9E9E9E),
    thumbColor: Color = Color.White,
    onIcon: Int? = null,
    offIcon: Int? = null,
    enabled: Boolean = true
) {
    //--------------------------- HANDLE BLINKING ---------------------------//
    val yellow = colorResource(R.color.yellow)
    val gray = Color(0xFFBBBBBB)
    val infiniteTransition = rememberInfiniteTransition(label = "blinkTransition")

    val blinkColor by infiniteTransition.animateColor(
        initialValue = yellow,
        targetValue = gray.copy(alpha = 0.6f),
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkColor"
    )

    val blinkAlpha by if (status == Status.CONNECTING) {
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "blinkAlpha"
        )
    } else remember { mutableFloatStateOf(1f) }

    val baseColor = when (status) {
        Status.CONNECTED -> onColor
        Status.CONNECTING -> blinkColor
        Status.DISCONNECTED -> offColor
    }
    val thumbBlink =
        if (status == Status.CONNECTING) thumbColor.copy(alpha = blinkAlpha.coerceAtLeast(0.7f)) else thumbColor

    val iconBlink =
        if (status == Status.CONNECTING) baseColor.copy(alpha = blinkAlpha) else baseColor
    //----------------------- END OF HANDLE BLINKING -----------------------//

    val thumbSize = (height - thumbPadding * 2).coerceAtLeast(8.dp)
    val maxOffsetPx = with(LocalDensity.current) {
        (width - thumbSize - thumbPadding * 2).toPx()
    }

    val progress by animateFloatAsState(
        targetValue = if (isOn) 1f else 0f,
        animationSpec = tween(250, easing = LinearEasing),
        label = "toggleProgress"
    )

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .background(
                color = baseColor,
                shape = CircleShape
            )
            .clickable(
                enabled = enabled,
                role = Role.Switch,
                onClick = { onToggle(!isOn) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // 🔹 Thumb animation
        Box(
            modifier = Modifier
                .offset { IntOffset((maxOffsetPx * progress).roundToInt(), 0) }
                .padding(thumbPadding)
                .size(thumbSize)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .background(
                    color = thumbBlink,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isOn && onIcon != null) {
                Icon(
                    painter = painterResource(onIcon),
                    contentDescription = "",
                    tint = iconBlink,
                    modifier = Modifier.size(thumbSize * 0.6f)
                )
            } else if (!isOn && offIcon != null) {
                Icon(
                    painter = painterResource(offIcon),
                    contentDescription = "",
                    tint = iconBlink,
                    modifier = Modifier.size(thumbSize * 0.6f)
                )
            }
        }
    }
}

