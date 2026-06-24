package com.tivanco.hymaco.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tivanco.hymaco.room.entities.AppLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert
    suspend fun insertLog(log: AppLogEntity)

    // گرفتن لاگ‌ها برای نمایش تو ترمینال (اینو از قبل داشتی)
    @Query("SELECT * FROM crane_logs ORDER BY id ASC")
    suspend fun getAllLogs(): List<AppLogEntity>

    @Query("DELETE FROM crane_logs")
    suspend fun deleteAllLogs()

    // ----------------- توابع جدید برای Store-and-Forward -----------------

    @Query("SELECT * FROM crane_logs ORDER BY id DESC LIMIT 500")
    fun getLatestLogsFlow(): Flow<List<AppLogEntity>>

    // ۱. گرفتن لاگ‌هایی که هنوز ارسال نشدن (مثلا حداکثر ۵۰۰ تا در هر بار)
    @Query("SELECT * FROM crane_logs WHERE isSynced = 0 ORDER BY id ASC LIMIT 500")
    suspend fun getUnsyncedLogs(): List<AppLogEntity>

    // ۲. آپدیت کردن وضعیت لاگ‌ها بعد از ارسال موفق به سرور
    @Query("UPDATE crane_logs SET isSynced = 1 WHERE id IN (:logIds)")
    suspend fun markLogsAsSynced(logIds: List<Int>)

    // ۳. پاکسازی هوشمند: پاک کردن لاگ‌های ارسال شده، به شرطی که جزو ۵۰۰ تای آخر (که تو UI میخوایم) نباشن
    @Query("""
        DELETE FROM crane_logs 
        WHERE isSynced = 1 
        AND id NOT IN (SELECT id FROM crane_logs ORDER BY id DESC LIMIT 500)
    """)
    suspend fun deleteSyncedAndOldLogs()
}