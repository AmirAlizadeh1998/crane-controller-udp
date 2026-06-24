package com.tivanco.hymaco.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tivanco.hymaco.room.entities.SystemLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemLogDao {
    @Insert
    suspend fun insert(log: SystemLogEntity)

    // گرفتن ۵۰ تا لاگ آخر برای نمایش تو UI (از جدید به قدیم)
    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<SystemLogEntity>>

    // پاک کردن لاگ‌های قدیمی که دیتابیس الکی پر نشه
    @Query("DELETE FROM system_logs")
    suspend fun clearLogs()
}