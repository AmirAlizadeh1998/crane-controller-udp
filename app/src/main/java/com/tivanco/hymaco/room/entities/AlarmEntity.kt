package com.tivanco.hymaco.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms_table")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val message: String,
    val time: Int, // همون ساعت کارکرد مثلا $3640$
    val persianDate: String
)