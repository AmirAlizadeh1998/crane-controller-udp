package com.tivanco.hymaco.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object AppPreferences {
    private const val PREF_NAME = "app_preferences"
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    // ---------- SAVE DATA ----------
    fun putString(context: Context, key: String, value: String) {
        getPrefs(context).edit { putString(key, value) }
    }
    // ---------- GET DATA ----------
    fun getString(context: Context, key: String, default: String = ""): String {
        return getPrefs(context).getString(key, default) ?: default
    }
    // ---------- CLEAR DATA ----------
    fun clear(context: Context) {
        getPrefs(context).edit { clear() }
    }
}