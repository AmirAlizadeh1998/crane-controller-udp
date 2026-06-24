package com.tivanco.hymaco.customViews

import android.media.SoundPool
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.tivanco.hymaco.R
import com.tivanco.hymaco.utils.responsiveSizes


private const val TAG = "LegButton"

@Composable
fun LegButton(
    modifier: Modifier = Modifier,
    icon: Int,
    onPress: () -> Unit = {},
    onRelease: () -> Unit = {},
    enabled: Boolean = true,
) {
    val context = LocalContext.current
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        if (pressed && enabled) 0.9f else 1f,
        label = "scaleAnim"
    )

    // SoundPool فقط اگر enabled باشد
    val soundPool = remember {
        SoundPool.Builder().setMaxStreams(2).build()
    }
    val soundPress = remember { soundPool.load(context, R.raw.button_press, 1) }
    val soundRelease = remember { soundPool.load(context, R.raw.button_release, 1) }

    Box(
        modifier = modifier
            .size(responsiveSizes().buttonSize)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .let {
                if (enabled) {
                    it.pointerInput(Unit) {
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
                } else it // ژست‌ها غیرفعال می‌شود
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = ""
        )
    }

}