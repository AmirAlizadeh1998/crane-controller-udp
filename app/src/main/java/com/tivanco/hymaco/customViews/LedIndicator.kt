package com.tivanco.hymaco.customViews

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tivanco.hymaco.R
import com.tivanco.hymaco.dataClass.Status
import com.tivanco.hymaco.utils.responsiveSizes

@Composable
fun LedIndicator(
    modifier: Modifier = Modifier,
    status: Status,
    isOn: Boolean = true
) {
    val (baseColor, _, _) = when (status) {
        Status.CONNECTED -> Triple(colorResource(R.color.green), Color.White, R.drawable.router_connect)
        Status.CONNECTING -> Triple(colorResource(R.color.yellow), colorResource(R.color.dark_gray), R.drawable.router)
        Status.DISCONNECTED -> Triple(colorResource(R.color.red), Color.White, R.drawable.router_disconnect)
    }

    // چشمک برای حالت CONNECTING
    val infiniteTransition = rememberInfiniteTransition(label = "blinkTransition")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    val alpha = when (status) {
        Status.CONNECTING -> blinkAlpha
        else -> if (isOn) 1f else 0.4f
    }

    // انیمیشن روشن بودن
    val scale by animateFloatAsState(if (isOn) 1.05f else 1f, label = "ledScale")
    val elevation by animateDpAsState(if (isOn) 12.dp else 4.dp, label = "ledShadow")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = modifier
                .size(responsiveSizes().ledSize)
                // 🔸 قاب بیرونی خاکستری
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFB0B0B0),
                            Color(0xFFE0E0E0),
                            Color(0xFFB0B0B0)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, 200f)
                    ),
                    shape = CircleShape
                )
                .border(width = 2.dp, color = Color.Gray, shape = CircleShape)
                .padding(6.dp), // فاصله بین قاب و LED
            contentAlignment = Alignment.Center
        ) {
            // 🔸 خود LED
            Box(
                modifier = Modifier
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .shadow(elevation, CircleShape, clip = false)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                baseColor.copy(alpha = alpha * 0.9f),
                                baseColor.copy(alpha = alpha),
                                baseColor.copy(alpha = alpha * 0.7f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(0f, 200f)
                        ),
                        shape = CircleShape
                    )
                    .size(responsiveSizes().ledSize * 0.8f),
                contentAlignment = Alignment.TopCenter
            ) {
                // Highlight درخشان بالا
                Box(
                    modifier = Modifier
                        .size(responsiveSizes().ledSize * 0.5f)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.4f),
                                    Color.Transparent
                                ),
                                center = Offset.Zero,
                                radius = 100f
                            ),
                            shape = CircleShape
                        )
                )
            }
//            Icon(
//                modifier = modifier
//                    .size(responsiveSizes().iconSize)
//                    .graphicsLayer(
//                        alpha = when (status) {
//                            Status.CONNECTING -> blinkAlpha
//                            else -> 1f
//                        }
//                    ),
//                painter = painterResource(icon),
//                contentDescription = "Wifi",
//                tint = iconColor,
//            )
        }
    }

}
