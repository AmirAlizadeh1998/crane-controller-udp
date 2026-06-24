package com.tivanco.hymaco.utils

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

fun playSound(context: Context, resId: Int) {
    try {
        val player = MediaPlayer.create(context, resId)
        player.setOnCompletionListener { it.release() }
        player.start()
    } catch (e: Exception) {
        Log.e("SoundManager", "Error playing sound", e)
    }
}