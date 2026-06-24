package com.tivanco.hymaco.viewModel.ai

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tivanco.hymaco.dataClass.ChatMessage
import com.tivanco.hymaco.dataClass.VoiceRecordState
import com.tivanco.hymaco.utils.voiceHandler.VoiceRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

class VoiceRecordViewModel : ViewModel() {

    var state by mutableStateOf(VoiceRecordState())
        private set

    private var timerJob: Job? = null

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    fun startRecording(context: Context) {
        VoiceRecorder.startRecording(context)

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var sec = 0
            while (true) {
                delay(1000)
                sec++
                state = state.copy(durationSeconds = sec)
            }
        }

        state = VoiceRecordState(
            isRecording = true,
            isRecorded = false,
            durationSeconds = 0,
            fileUri = null
        )
    }

    fun stopRecording() {
        val uri = VoiceRecorder.stopRecording()
        timerJob?.cancel()

        state = state.copy(
            isRecording = false,
            isRecorded = true,
            fileUri = uri
        )
    }

    fun reset() {
        timerJob?.cancel()
        state = VoiceRecordState()
    }

    fun deleteRecording() {
        state.fileUri?.path?.let { File(it).delete() }
        reset()
    }

    fun sendVoiceMessage(file: File) {
        viewModelScope.launch {
            _isSending.value = true

            val userMsg = ChatMessage.VoiceMessage(
                uri = Uri.fromFile(file),
                duration = 0,
                amplitudes = emptyList(),
                fromUser = true
            )
            _messages.value = _messages.value + userMsg

            try {
                val response = uploadVoiceToServer(file)
                val botMsg = ChatMessage.TextMessage(response, fromUser = false)
                _messages.value += botMsg
            } catch (_: Exception) {
                _messages.value += ChatMessage.TextMessage("خطا در ارسال فایل صوتی", false)
            }

            _isSending.value = false
        }
    }

    private suspend fun uploadVoiceToServer(file: File): String = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("voice", file.name,
                    file.asRequestBody("audio/mpeg".toMediaType()))
                .build()

            val request = Request.Builder()
                .url("http://tivan-smart.ir/agent/chat.php")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body.string()

            return@withContext JSONObject(body).optString("reply", "پاسخ نامشخص")
        } catch (_: Exception) {
            "خطا در اتصال"
        }
    }
}
