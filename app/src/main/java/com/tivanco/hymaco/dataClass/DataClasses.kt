package com.tivanco.hymaco.dataClass

import android.net.Uri
import androidx.compose.ui.graphics.Color
import java.util.UUID

/*********************************
 ******** SEALED CLASSES *********
 *********************************/
sealed class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    open val fromUser: Boolean
) {
    data class TextMessage(
        val text: String,
        override val fromUser: Boolean,
        val isTyping: Boolean = false
    ) : ChatMessage(fromUser = fromUser)

    data class FileMessage(
        val uri: Uri,
        val caption: String? = null,
        val isImage: Boolean,
        override val fromUser: Boolean
    ) : ChatMessage(fromUser = fromUser)

    data class VoiceMessage(
        val uri: Uri?,
        val duration: Long,
        val amplitudes: List<Float> = emptyList(),
        override val fromUser: Boolean
    ) : ChatMessage(fromUser = fromUser)
}

sealed class Screen(val route: String) {
    data object Home : Screen("Home")
    data object Settings : Screen("Settings") /*{
        fun routeToSettings(modemIP: String) = "Settings/$modemIP"
    }*/
    data object Support : Screen("Support")
    data object Logs : Screen("Logs")
    data object AppLogs : Screen("AppLogs")
}

sealed class SendCommand(val data: ByteArray) {
    data class KeyState(val payload: String) : SendCommand(payload.toByteArray(Charsets.US_ASCII))
    data class TextMessage(val payload: String) : SendCommand(payload.toByteArray(Charsets.US_ASCII))
}

/*********************************
 ********* DATA CLASSES **********
 *********************************/
data class VoiceUiState(
    val isRecording: Boolean = false,
    val durationSec: Int = 0,
    val amplitude: Int = 0,
    val filePath: String? = null,
    val error: String? = null
)

data class VoiceRecordState(
    val isRecording: Boolean = false,
    val isRecorded: Boolean = false, // ضبط شده و آماده ارسال
    val durationSeconds: Int = 0,
    val fileUri: Uri? = null
)

data class DialogButton(
    val text: String,
    val onClick: () -> Unit
)

data class SettingsData(
    val ssid: String,
    val pass: String,
    val interval: String,
    val mqttHost: String,
    val mqttPort: String,
    val customName: String
)

data class ToastData(
    val message: String,
    val iconRes: Int? = null,
    val iconColor: Color = Color.Unspecified,
    val backgroundColor: Color = Color.Black.copy(alpha = 0.7f),
    val textColor: Color = Color.White,
    val duration: Long = 2000L,
    val id: Long = System.nanoTime()
)

data class PreferencesKeys(
    val wifiSsid: String = "wifiSsid",
    val wifiPass: String = "wifiPass",
    val interval: String = "interval",
    val mqttHost: String = "mqttHost",
    val mqttPort: String = "mqttPort",
    val customCraneName: String = "custom_crane_name",
    val craneSettings: String = "crane_settings",
    val imei: String = "hardware_id",
)

data class LogType(
    val error: String = "ERROR",
    val craneAlarm: String = "CRANE_ALARM",
    val received: String = "RECEIVED",
    val sent: String = "SENT",
    val info: String = "INFO",
)

data class CraneCommand(
    var clutch: Int = 0,
    var turn: Int = 0,
    var lift: Int = 0,
    var telescope: Int = 0,
    var fixFront: Int = 0,
    var fixBack: Int = 0,
    var rpm: Int = 0,
    var start: Int = 0,
    var bough: Int = 0
)

data class ErrorItem(
    val message: String,
    val time: String,
    val persianDate: String,
)

/*********************************
 ********* ENUM CLASSES **********
 *********************************/
enum class Status { DISCONNECTED, CONNECTING, CONNECTED }

enum class SendMode { STATUS, ZERO, WAITING_RESPONSE, NONE }

object Constants {
    const val MICROSWITCH = "میکروسوئیچ"
    const val OIL_CHANGE = "تعویض روغن"
}

enum class ConnectionEvent {
    CONNECTED,
    DISCONNECTED
}