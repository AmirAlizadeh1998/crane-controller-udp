package com.tivanco.hymaco.room

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tivanco.hymaco.retrofit.RetrofitInstance
import com.tivanco.hymaco.retrofit.SyncLogRequest
import com.tivanco.hymaco.room.entities.SystemLogEntity
import com.tivanco.hymaco.utils.PrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // متغیرهای دیتابیس و شبکه
        val context = applicationContext
        val database = AppDatabase.getDatabase(context)
        val logDao = database.logDao()
        val systemLogDao = database.systemLogDao()
        val apiService = RetrofitInstance.api

        try {
            // ۱. ثبت بیدار شدن Worker در همان ابتدا
            systemLogDao.insert(
                SystemLogEntity(
                    message = "Worker شروع به کار کرد...",
                    status = "INFO"
                )
            )

            // ۲. گرفتن لاگ‌های ارسال نشده
            val unsyncedLogs = logDao.getUnsyncedLogs()

            if (unsyncedLogs.isEmpty()) {
                Log.d("LogSyncWorker", "لاگ جدیدی برای ارسال وجود ندارد.")
                // اینجا هم تو سیستم لاگ بنویسیم که کاربر بدونه Worker کارشو کرده ولی دیتایی نبوده
                systemLogDao.insert(
                    SystemLogEntity(
                        message = "لاگ جدیدی برای همگام‌سازی یافت نشد.",
                        status = "INFO"
                    )
                )
                return@withContext Result.success()
            }

            // ۳. گرفتن هویت دستگاه (فقط یک بار قبل از حلقه برای سرعت بیشتر)
            val deviceId = PrefsManager.getImei(context)
            val craneName = PrefsManager.getCustomName(context)

            // ۴. تکه‌تکه کردن (بسته‌های ۵۰۰ تایی)
            val chunks = unsyncedLogs.chunked(500)
            var totalSentCount = 0 // برای اینکه در نهایت بشماریم چندتا فرستادیم

            for (chunk in chunks) {
                // ۵. ساختن بسته پستی فقط برای همین ۵۰۰ تای فعلی
                val request = SyncLogRequest(
                    hardwareId = deviceId,
                    customName = craneName,
                    logs = chunk
                )

                // ۶. ارسال به سرور
                val response = apiService.sendLogsToServer(request)

                // ۷. بررسی جواب سرور
                if (response.isSuccessful && response.body()?.success == true) {
                    // تیک ارسال این بسته رو بزن
                    val sentIds = chunk.map { it.id }
                    logDao.markLogsAsSynced(sentIds)

                    totalSentCount += chunk.size
                    Log.d("LogSyncWorker", "تعداد ${chunk.size} لاگ با موفقیت ارسال شد.")

                } else {
                    val body = response.body()
                    val errorMsg = "خطای سرور (کد ${response.code()})"

                    // اگر سرور پیام خطایی فرستاده باشد، آن را نشان بده
                    val detailedMsg = body?.message ?: "پیامی از سرور دریافت نشد"

                    Log.e("LogSyncWorker", "$errorMsg: $detailedMsg")

                    systemLogDao.insert(
                        SystemLogEntity(
                            message = "$errorMsg: $detailedMsg",
                            status = "ERROR"
                        )
                    )

                    return@withContext Result.retry()
                }
            }

            // ۸. اگه کل حلقه موفقیت‌آمیز بود، یک پیام کلی تو سیستم لاگ می‌نویسیم
            systemLogDao.insert(
                SystemLogEntity(
                    message = "تعداد $totalSentCount لاگ در ${chunks.size} بسته با موفقیت ارسال شد.",
                    status = "SUCCESS"
                )
            )

            // ۹. آشغال‌روبی! لاگ‌های سینک شده و قدیمی رو پاک کن
            logDao.deleteSyncedAndOldLogs()

            return@withContext Result.success()

        } catch (e: Exception) {
            // خطای اینترنت، تایم‌اوت یا کرش غیرمنتظره
            systemLogDao.insert(
                SystemLogEntity(
                    message = "خطا در اتصال یا ارسال: ${e.localizedMessage}",
                    status = "ERROR"
                )
            )
            Log.e("LogSyncWorker", "خطای شبکه یا ناشناخته: ${e.message}")

            return@withContext Result.retry()
        }
    }
}