package com.tivanco.hymaco.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tivanco.hymaco.room.entities.AlarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    // گرفتن همه خطاها به صورت زنده (Flow)
    @Query("SELECT * FROM alarms_table ORDER BY id DESC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    // ذخیره خطای جدید
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlarm(alarm: AlarmEntity)

    // پاک کردن کل تاریخچه (برای دکمه حذف خطاها)
    @Query("DELETE FROM alarms_table")
    suspend fun deleteAllAlarms()
}
