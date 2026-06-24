package com.tivanco.hymaco.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tivanco.hymaco.utils.getCurrentJalaliDate

@Entity(tableName = "crane_logs")
data class AppLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: String = getCurrentJalaliDate(), // زمان دقیق دریافت/ارسال پکت
    val logType: String, // مثلا "UDP_TX" (ارسال)، "UDP_RX" (دریافت)، "ERROR"، "INFO"
    val message: String,  // محتوای پکت یا متن ارور
    val isSynced: Boolean = false // بررسی میکنه که دیتا به سرور ارسال شده یا نه
)