package com.tivanco.hymaco.activityAndApplication

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.tivanco.hymaco.room.LogSyncWorker
import com.tivanco.hymaco.viewModelFactory.AppConstructor
import com.tivanco.hymaco.viewModelFactory.AppContainer
import java.util.concurrent.TimeUnit

class CraneApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        setupLogSyncWorker()
        container = AppConstructor(this)
    }

    private fun setupLogSyncWorker() {
        // شرط می‌ذاریم: فقط وقتی کار کن که اینترنت وصل باشه
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // می‌گیم هر ۱۵ دقیقه یکبار اجرا شو (اگه اینترنت قطع باشه صبر میکنه تا وصل شه)
        val syncRequest = PeriodicWorkRequestBuilder<LogSyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        // ثبت تو سیستم اندروید (KEEP یعنی اگه از قبل ثبت شده بود، کاریش نداشته باش)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "PeriodicLogSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
