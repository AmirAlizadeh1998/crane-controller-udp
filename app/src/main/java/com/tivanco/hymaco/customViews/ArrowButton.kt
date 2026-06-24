package com.tivanco.hymaco.customViews

import android.media.SoundPool
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tivanco.hymaco.R
import com.tivanco.hymaco.utils.responsiveSizes
import kotlinx.coroutines.delay

@Composable
fun ArrowButton(
    modifier: Modifier = Modifier,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    iconResource: Int,
    desc: String = "",
    enabled: Boolean = true,
) {
    val context = LocalContext.current
    // ساخت SoundPool و بارگذاری صداها
    val soundPool = remember {
        SoundPool.Builder().setMaxStreams(2).build()
    }
    val soundPress = remember { soundPool.load(context, R.raw.button_press, 1) }
    val soundRelease = remember { soundPool.load(context, R.raw.button_release, 1) }

    var pressed by remember { mutableStateOf(false) }

    LaunchedEffect(pressed) {
        while (pressed && enabled) {
            onPress()
            delay(150)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        label = "scaleAnim"
    )

    Box(
        modifier = modifier
            .size(responsiveSizes().arrowSize)
            .alpha(if (enabled) 1f else 0.5f)
            .then(
                if (enabled) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                pressed = true
                                onPress()
                                soundPool.play(soundPress, 1f, 1f, 1, 0, 1f)
                                try {
                                    awaitRelease()
                                } finally {
                                    onRelease()
                                    pressed = false
                                    soundPool.play(soundRelease, 1f, 1f, 1, 0, 1f)
                                }

                            }
                        )
                    }
                } else Modifier // اگر غیرفعال است هیچ gestureی ثبت نشود
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconResource),
            contentDescription = desc,
            tint = if (enabled) Color.Black else Color.Gray,
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
                .size(100.dp),
        )
    }
}