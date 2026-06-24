package com.tivanco.hymaco.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.edit
import com.tivanco.hymaco.dataClass.PreferencesKeys

object PrefsManager {
    val prefKey = PreferencesKeys()

    // ۱. گرفتن آیدی سخت‌افزاری (ثابت برای هر تبلت/گوشی)
    @SuppressLint("HardwareIds")
    fun getImei(context: Context): String {
        val prefs = context.getSharedPreferences(prefKey.craneSettings, Context.MODE_PRIVATE)
        return prefs.getString(prefKey.imei, "") ?: ""
    }

    fun saveImei(context: Context, newName: String) {
        val prefs = context.getSharedPreferences(prefKey.craneSettings, Context.MODE_PRIVATE)
        prefs.edit { putString(prefKey.imei, newName) }
    }

    // ۲. گرفتن اسم دلخواه کاربر
    fun getCustomName(context: Context): String {
        val prefs = context.getSharedPreferences(prefKey.craneSettings, Context.MODE_PRIVATE)
        return prefs.getString(prefKey.customCraneName, "") ?: ""
    }

    // ۳. ذخیره اسم جدیدی که کاربر تو UI وارد میکنه
    fun saveCustomName(context: Context, newName: String) {
        val prefs = context.getSharedPreferences(prefKey.craneSettings, Context.MODE_PRIVATE)
        prefs.edit { putString(prefKey.customCraneName, newName) }
    }
}
