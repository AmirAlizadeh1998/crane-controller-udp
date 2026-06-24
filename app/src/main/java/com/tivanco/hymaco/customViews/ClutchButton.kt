package com.tivanco.hymaco.customViews

import android.media.SoundPool
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tivanco.hymaco.R
import com.tivanco.hymaco.utils.responsiveSizes
import com.tivanco.hymaco.viewModel.MainUiViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

@Composable
fun PushButton(
    modifier: Modifier = Modifier,
    text: String,
    onPress: () -> Unit = {},
    onRelease: () -> Unit = {},
    enabled: Boolean,
    uiViewModel: MainUiViewModel
) {
    val context = LocalContext.current
    var pressed by remember { mutableStateOf(false) }
    val btnReadyVm = uiViewModel.btnReady.collectAsState().value
    var isPressing by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        if (pressed && enabled) 0.9f else 1f,
        label = "scaleAnim"
    )

    val elevation by animateDpAsState(
        if (pressed && enabled) 2.dp else 8.dp,
        label = "shadowAnim"
    )

    val soundPool = remember {
        SoundPool.Builder().setMaxStreams(2).build()
    }
    val soundPress = remember { soundPool.load(context, R.raw.button_press, 1) }
    val soundRelease = remember { soundPool.load(context, R.raw.button_release, 1) }

    LaunchedEffect(btnReadyVm) {
        if (!btnReadyVm) {
            pressed = false
            isPressing = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(responsiveSizes().buttonSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(responsiveSizes().buttonSize)
                .border(4.dp, Color(0xFF80978F), RoundedCornerShape(16.dp))
                .padding(3.dp)
                .background(Color.Black, shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(responsiveSizes().buttonSize)
                    .border(1.dp, Color.Black, RoundedCornerShape(10.dp))
                    .padding(3.dp)
                    .background(Color.Black, shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(responsiveSizes().buttonSize)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale
                        )
                        .shadow(elevation, RoundedCornerShape(10.dp), clip = false)
                        .background(
                            brush = Brush.linearGradient(
                                colors =
                                    when {
                                        enabled && pressed ->
                                            listOf(
                                                Color(0xFF66BB6A),
                                                Color(0xFF00A14D),
                                                Color(0xFF2E7D32)
                                            )

                                        enabled ->
                                            listOf(
                                                Color(0xFFF4E285),
                                                Color(0xFFE6C229),
                                                Color(0xFFCDA434)
                                            )

                                        else ->
                                            listOf(
                                                Color(0xFF9E9E9E),
                                                Color(0xFF757575),
                                                Color(0xFF616161)
                                            )
                                    },
                                start = Offset(0f, 0f),
                                end = Offset(0f, 200f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .let {
                            if (enabled) {
                                it.pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            if (!isPressing) {
                                                isPressing = true
//                                                coroutineScope {
//                                                    delay(100)
//                                                }
                                                onPress()
                                                pressed = true
                                                soundPool.play(
                                                    soundPress,
                                                    1f,
                                                    1f,
                                                    1,
                                                    0,
                                                    1f
                                                )
                                                try {
                                                    awaitRelease()
                                                } finally {
//                                                    coroutineScope {
//                                                        delay(100)
                                                        onRelease()
//                                                    }
                                                    pressed = false
                                                    soundPool.play(
                                                        soundRelease,
                                                        1f,
                                                        1f,
                                                        1,
                                                        0,
                                                        1f
                                                    )
//                                                    coroutineScope {
//                                                        delay(100)
//                                                    }
                                                    isPressing = false
                                                }
                                            }
                                        },
                                    )
                                }
                            } else it // ژست‌ها غیرفعال می‌شود
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = text,
                        textAlign = TextAlign.Center,
                        fontSize = responsiveSizes().fontSize,
                        color = when {
                            enabled && pressed -> Color.White
                            enabled -> Color.Black
                            else -> Color.LightGray.copy(alpha = 0.7f)
                        }
                    )
                }
            }
        }
    }
}