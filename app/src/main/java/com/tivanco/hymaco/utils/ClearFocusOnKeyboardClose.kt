package com.tivanco.hymaco.utils

import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

@Composable
fun ClearFocusOnKeyboardClose(
    onKeyboardClosed: () -> Unit
) {
    val rootView = LocalView.current
    val keyboardVisible = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            val isKeyboardNowVisible = keypadHeight > screenHeight * 0.15
            if (keyboardVisible.value && !isKeyboardNowVisible) {
                onKeyboardClosed()
            }
            keyboardVisible.value = isKeyboardNowVisible
        }

        rootView.viewTreeObserver.addOnGlobalLayoutListener(listener)

        onDispose {
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
}
