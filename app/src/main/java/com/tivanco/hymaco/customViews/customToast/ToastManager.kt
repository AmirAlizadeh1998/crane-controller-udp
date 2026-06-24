package com.tivanco.hymaco.customViews.customToast

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.tivanco.hymaco.dataClass.ToastData

class ToastManager {
    private val _toasts = mutableStateListOf<ToastData>()
    val toasts: List<ToastData> = _toasts
    fun showToast(toast: ToastData) {
        _toasts.add(toast)
    }
    fun removeToast(toast: ToastData) {
        _toasts.remove(toast)
    }
}
val LocalToastManager = staticCompositionLocalOf { ToastManager() }