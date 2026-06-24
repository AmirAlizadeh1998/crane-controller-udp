package com.tivanco.hymaco.appUI.settingsScreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tivanco.hymaco.utils.responsiveSizes

@Composable
fun SettingsBottomBar(onResetClick: () -> Unit) {
//    val context = LocalContext.current
//    val TAG = "SettingsBottomBar"
//     // ساخت SoundPool و بارگذاری صداها
//    val soundPool = remember {
//        SoundPool.Builder().setMaxStreams(2).build()
//    }
//    val soundPress = remember { soundPool.load(context, R.raw.button_press, 1) }
//    val soundRelease = remember { soundPool.load(context, R.raw.button_release, 1) }
//
//    LaunchedEffect(isPressed) {
//        if (isPressed) {
//            soundPool.play(soundPress, 1f, 1f, 1, 0, 1f)
//        } else {
//            soundPool.play(soundRelease, 1f, 1f, 1, 0, 1f)
//        }
//    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "scaleAnim")

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = if (isPressed) 2.dp else 8.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isPressed)
                        listOf(
                            Color(0xFF3F4E64),
                            Color(0xFF4B5C75),
                            Color(0xFF3F4E64)
                        )
                    else
                        listOf(
                            Color(0xFF738BB0),
                            Color(0xFF4E5E78),
                            Color(0xFF3F4E64)
                        )
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Black
        ),
        interactionSource = interactionSource,
        onClick = onResetClick
    ) {
        Text(
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                ),
            text = "بازنشانی تنظیمات",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = responsiveSizes().fontSize
        )
    }
}