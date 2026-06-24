package com.tivanco.hymaco.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tivanco.hymaco.dataClass.LogType
import com.tivanco.hymaco.repository.UdpRepository
import com.tivanco.hymaco.room.dao.LogDao
import com.tivanco.hymaco.room.dao.SystemLogDao
import com.tivanco.hymaco.room.entities.AppLogEntity
import com.tivanco.hymaco.room.entities.SystemLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "CraneLogViewModel"

class CraneLogViewModel(repository: UdpRepository, private val logDao: LogDao, sysLogDao: SystemLogDao): ViewModel() {
    private val logType = LogType()
    val host = repository.host
    val connectionStatus = repository.connectionStatus
    private fun saveLogToDatabase(type: String) {
        // حتما لانچ میکنیم تو IO که یه میلی‌ثانیه هم جلوی دست و پای UDP رو نگیره
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // ساخت لاگ جدید (زمان تو خود Entity خودکار شمسی میخوره)
                val newLog = AppLogEntity(logType = type, message = "لاگ‌های سیستم توسط کاربر پاک شد")
                logDao.insertLog(newLog)

                // پاکسازی دیتابیس بر اساس تعداد به جای زمان (ورودی دو ساعت پیش رو حذف کردیم)
                // این تابع الان فقط ۵۰۰ لاگ آخر رو نگه میداره (با توجه به آپدیت DAO تو پیام قبلی)
                logDao.deleteSyncedAndOldLogs()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to save log to DB", e)
            }
        }
    }

    // گرفتن لاگ‌ها به صورت زنده از دیتابیس برای نمایش تو UI
    val logs: StateFlow<List<AppLogEntity>> = logDao.getLatestLogsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // این عدد عالیه، منابع رو هدر نمیده
            initialValue = emptyList()
        )

    val sysLogs: StateFlow<List<SystemLogEntity>> = sysLogDao.getRecentLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            // برای پاک کردن صفحه نمایش، کل لاگ‌های دیتابیس رو پاک می‌کنیم
            logDao.deleteAllLogs()

            // لاگ خودکار سیستم رو اضافه می‌کنیم که بدونیم دستی پاک شده
            saveLogToDatabase(logType.info)
        }
    }
}