package com.tivanco.hymaco.retrofit

import com.google.gson.annotations.SerializedName
import com.tivanco.hymaco.room.entities.AppLogEntity

// دیتایی که قراره بفرستیم سمت سرور
data class SyncLogRequest(
    @SerializedName("hardware_id") val hardwareId: String,
    @SerializedName("custom_name") val customName: String,
    @SerializedName("logs") val logs: List<AppLogEntity>
)

// جوابی که از سرور (فایل PHP) می‌گیریم
data class SyncLogResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("received_count")
    val receivedCount: Int?
)
