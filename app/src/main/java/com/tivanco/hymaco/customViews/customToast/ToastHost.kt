package com.tivanco.hymaco.customViews.customToast

import androidx.compose.runtime.Composable
import kotlin.collections.first
import kotlin.collections.isNotEmpty

@Composable
fun ToastHost(
    toastManager: ToastManager
) {
    val toasts = toastManager.toasts

    if (toasts.isNotEmpty()) {
        val currentToast = toasts.first()

        // استفاده از یک ID منحصر به فرد برای هر toast
        CustomToast(
            key = currentToast.id,
            message = currentToast.message,
            iconRes = currentToast.iconRes,
            backgroundColor = currentToast.backgroundColor,
            contentColor = currentToast.textColor,
            duration = currentToast.duration,
            onDismiss = {
                toastManager.removeToast(currentToast)
            }
        )
    }
}