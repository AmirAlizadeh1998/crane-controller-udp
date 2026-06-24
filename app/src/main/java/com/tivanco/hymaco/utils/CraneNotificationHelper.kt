package com.tivanco.hymaco.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object CraneNotificationHelper {
    private const val CHANNEL_ID = "crane_alarms_channel"
    private var notificationId = 1000 // برای اینکه نوتیفیکیشن‌ها رو هم نیفتن و جدا باشن

    fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // برای اندروید 8 (API 26) به بالا باید Channel بسازیم
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "هشدارهای جرثقیل",
                NotificationManager.IMPORTANCE_HIGH // اهمیت بالا که صدا بده و پاپ‌آپ بشه
            ).apply {
                description = "نمایش خطاهای مهم دستگاه"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // اینجا می‌تونی آیکون اپ خودت رو بذاری مثلا R.drawable.ic_launcher_foreground
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false) // کاربر روی نوتیفیکیشن کلیک کرد پاک نشه (مگر اینکه سوایپ کنه)
            // .setOngoing(true) // اگه اینو از کامنت در بیاری، کاربر اصلا نمیتونه نوتیفیکیشن رو با سوایپ پاک کنه!
            .build()

        // ارسال نوتیفیکیشن
        notificationManager.notify(notificationId++, notification)
    }
}
