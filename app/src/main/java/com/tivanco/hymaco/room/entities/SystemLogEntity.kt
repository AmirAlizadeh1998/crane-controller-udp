package com.tivanco.hymaco.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tivanco.hymaco.utils.getCurrentJalaliDate

@Entity(tableName = "system_logs")
data class SystemLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: String = getCurrentJalaliDate(),
    val message: String,
    val status: String // مثلا: "INFO", "SUCCESS", "ERROR"
)