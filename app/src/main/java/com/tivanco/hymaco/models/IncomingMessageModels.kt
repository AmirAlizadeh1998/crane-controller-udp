package com.tivanco.hymaco.models

import android.util.Log

// ۱. مدل‌های ساختاریافته برای پیام‌های دریافتی
sealed class IncomingMessage {
    data class ConnectOk(val imei: String) : IncomingMessage()
    object DisconnectOk : IncomingMessage()
    data class StatusUpdate(val rawMessage: String) : IncomingMessage()
    data class Generic(val rawMessage: String) : IncomingMessage()
}

// ۲. خود آبجکت پارسر (چون حالت (State) نداره، object بهترین گزینه‌ست)
object UdpMessageParser {
    fun parse(rawMessage: String): IncomingMessage {
        return when {
            rawMessage.startsWith("connect_ok:") -> {
                val imei = rawMessage.substringAfter(":")
                IncomingMessage.ConnectOk(imei)
            }
            rawMessage.startsWith("disconnect_ok") -> {
                IncomingMessage.DisconnectOk
            }
            rawMessage.startsWith("status") -> {
                Log.d("UdpMessageParser", "raw message: $rawMessage")
                IncomingMessage.StatusUpdate(rawMessage)
            }
            else -> {
                IncomingMessage.Generic(rawMessage)
            }
        }
    }
}
