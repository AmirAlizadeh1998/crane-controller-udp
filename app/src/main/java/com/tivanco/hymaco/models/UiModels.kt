package com.tivanco.hymaco.models

import com.tivanco.hymaco.room.entities.AlarmEntity

data class CraneUiState(
    val isConnected: Boolean = false,
    val connectionPersianTime: String = "",
    val statusData: CraneStatus? = null, // دیتاهای پارس شده جرثقیل
    val alarms: List<AlarmEntity> = emptyList(), // لیست خطاها
    val isLogMode: Boolean = false
)