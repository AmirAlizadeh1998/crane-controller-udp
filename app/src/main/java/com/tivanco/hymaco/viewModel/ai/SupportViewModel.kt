package com.tivanco.hymaco.viewModel.ai

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tivanco.hymaco.dataClass.ChatMessage
import com.tivanco.hymaco.dataClass.VoiceUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Random
import java.util.concurrent.TimeUnit
import javax.net.SocketFactory
import kotlin.math.sin

class SupportViewModel(application: Application) : AndroidViewModel(application) {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isSending = MutableStateFlow(false)

    // کلاینت HTTP با تنظیمات بهینه
    private var httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val connectivityManager by lazy {
        getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    // تغییر اسم متغیر از cellularNetworkCallback به activeNetworkCallback
    private var activeNetworkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        // به محض ساخته شدن ویومدل، می‌ریم سراغ اتصال به یک شبکه اینترنت‌دار
        bindOkHttpToInternet()
    }

    private var currentBoundNetwork: Network? = null

    private fun bindOkHttpToInternet() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) // حتما اینترنت داشته باشه
            .build()

        activeNetworkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)

                // چک می‌کنیم که آیا سیستم‌عامل اینترنت رو تایید کرده (پینگ داره) یا نه
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {

                    // اگه قبلاً به همین شبکه بایند شدیم، دیگه نیازی نیست دوباره OkHttpClient رو بسازیم (بهینه‌سازی)
                    if (currentBoundNetwork == network) return
                    currentBoundNetwork = network

                    // پیدا کردن نوع شبکه برای لاگ زدن
                    val isWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    val netName = if (isWifi) "وای‌فای" else "دیتای موبایل"

                    Log.d(TAG, "شبکه اینترنت‌دار و تایید شده ($netName) پیدا شد! اتصال OkHttp...")

                    // بازسازی کلاینت OkHttp با نتورک جدید (وای‌فای یا دیتا)
                    httpClient = httpClient.newBuilder()
                        .socketFactory(network.socketFactory)
                        // اجبار OkHttp به انجام DNS Lookup روی همین شبکه تایید شده
                        .dns { hostname ->
                            try {
                                Log.d(TAG, "در حال پیدا کردن IP برای $hostname از طریق $netName...")
                                network.getAllByName(hostname).toList()
                            } catch (e: Exception) {
                                Log.e(TAG, "خطای DNS روی شبکه", e)
                                okhttp3.Dns.SYSTEM.lookup(hostname)
                            }
                        }
                        .build()
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)

                // فقط اگه همون شبکه‌ای که بهش وصل بودیم قطع شد، برمی‌گردیم به تنظیمات پیش‌فرض
                if (currentBoundNetwork == network) {
                    Log.d(TAG, "شبکه اینترنت‌دار قطع شد! بازگشت به تنظیمات پیش‌فرض OkHttp")
                    currentBoundNetwork = null

                    // اگه اینترنت قطع شد، برمی‌گردونیم به حالت پیش‌فرض
                    httpClient = httpClient.newBuilder()
                        .socketFactory(SocketFactory.getDefault())
                        .dns(okhttp3.Dns.SYSTEM)
                        .build()
                }
            }
        }

        connectivityManager.requestNetwork(request, activeNetworkCallback!!)
    }

    companion object {
        private const val TAG = "SupportViewModel"
        private const val BASE_URL = "https://tivan-smart.ir/agent/gapgpt"
        private const val TEXT_API_URL = "$BASE_URL/chat-text.php"
        private const val VOICE_API_URL = "$BASE_URL/chat-voice.php"
        private const val FILE_API_URL = "$BASE_URL/chat-file.php"
    }

    private fun addMessage(message: ChatMessage) {
        _messages.value = _messages.value.toMutableList().apply {
            add(message)
        }
    }

    // ------------------- TEXT HANDLER -------------------
    fun sendTextMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isSending.value = true

            // پیام کاربر
            _messages.value = _messages.value + ChatMessage.TextMessage(
                text = text,
                fromUser = true
            )

            // پیام موقت AI (در حال تایپ)
            val typingMessage = ChatMessage.TextMessage(
                text = "در حال تایپ...",
                fromUser = false,
                isTyping = true
            )

            _messages.value = _messages.value + typingMessage

            try {
                val response = withContext(Dispatchers.IO) {
                    val formBody = FormBody.Builder()
                        .add("message", text)
                        .build()

                    val request = Request.Builder()
                        .url(TEXT_API_URL)
                        .post(formBody)
                        .build()

                    val resp = httpClient.newCall(request).execute()

                    if (!resp.isSuccessful) {
                        throw Exception("خطای سرور (${resp.code})")
                    }

                    val body = resp.body.string()
                    Log.d(TAG, "SERVER RESPONSE: $body")
                    val json = JSONObject(body)

                    json.optString("reply", "پاسخی یافت نشد")
                }

                // جایگزین کردن پیام typing
                _messages.value = _messages.value.map {
                    if (it.id == typingMessage.id)
                        ChatMessage.TextMessage(
                            text = response,
                            fromUser = false
                        )
                    else it
                }

            } catch (e: Exception) {
                Log.e(TAG, "sendTextMessage error", e)

                _messages.value = _messages.value.map {
                    if (it.id == typingMessage.id)
                        ChatMessage.TextMessage(
                            text = "❌ خطا در دریافت پاسخ",
                            fromUser = false
                        )
                    else it
                }

            } finally {
                _isSending.value = false
            }
        }
    }

    // ------------------- VOICE HANDLER -------------------
    private val _voiceState = MutableStateFlow(VoiceUiState())
    val voiceState = _voiceState.asStateFlow()

    private var recorder: MediaRecorder? = null
    private var recordJob: Job? = null

    fun startRecording() {
        val context = getApplication<Application>()
        val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")

        try {
            recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(context) // اندروید 12 (API 31) به بالا
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder() // نسخه‌های قدیمی‌تر
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            // اینجا آدرس فایل رو تو استیت ذخیره می‌کنیم
            _voiceState.value = VoiceUiState(
                isRecording = true,
                filePath = file.absolutePath
            )

            recordJob = viewModelScope.launch {
                try {
                    while (isActive) {
                        delay(60)
                        _voiceState.update {
                            it.copy(
                                durationSec = it.durationSec + 1,
                                amplitude = recorder?.maxAmplitude ?: 0
                            )
                        }
                    }
                } catch (e: Exception) {
                    _voiceState.update { it.copy(error = e.message) }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطا در شروع ضبط صدا", e)
            _voiceState.update { it.copy(error = "خطا در دسترسی به میکروفون") }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            try {
                recordJob?.cancel()

                recorder?.apply {
                    stop()
                    release()
                }

                _voiceState.update {
                    it.copy(
                        isRecording = false,
                        // filePath همون قبلی می‌مونه تا بتونیم ارسالش کنیم
                        filePath = it.filePath
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "خطا در توقف ضبط", e)
                _voiceState.update {
                    it.copy(
                        isRecording = false,
                        error = "ضبط ویس ناموفق بود"
                    )
                }
            } finally {
                recorder = null
            }
        }
    }

    fun clearVoiceState() {
        val currentPath = _voiceState.value.filePath
        if (currentPath != null) {
            val file = File(currentPath)
            if (file.exists()) file.delete()
        }
        _voiceState.value = VoiceUiState() // ریست کردن کامل
    }

    fun sendVoiceMessage(
        uri: Uri,
        amplitudes: List<Float>,
        duration: Long
    ) {
        val filePathToUpload = _voiceState.value.filePath ?: return
        val file = File(filePathToUpload)

        Log.d(TAG, "=================================")
        Log.d(TAG, "مسیر دقیق فایل: ${file.absolutePath}")
        Log.d(TAG, "حجم فایل قبل از ارسال: ${file.length()} بایت")
        Log.d(TAG, "=================================")
        _voiceState.value = VoiceUiState()

        viewModelScope.launch {
            _isSending.value = true

            val processedAmplitudes = normalizeAndDownsampleAmplitudes(amplitudes)

            val userVoiceMessage = ChatMessage.VoiceMessage(
                uri = uri,
                duration = duration,
                amplitudes = processedAmplitudes,
                fromUser = true
            )
            addMessage(userVoiceMessage)

            val typingMessage = ChatMessage.TextMessage(
                text = "در حال بررسی صدا...",
                fromUser = false,
                isTyping = true
            )
            addMessage(typingMessage)

            try {
                val response = withContext(Dispatchers.IO) {
                    val file = File(filePathToUpload)

                    if (!file.exists()) {
                        throw Exception("فایل صوتی برای ارسال یافت نشد!")
                    }

                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                            "voice",
                            file.name,
                            file.asRequestBody("audio/mp4".toMediaType())
                        )
                        .build()

                    val request = Request.Builder()
                        .url(VOICE_API_URL)
                        .post(requestBody)
                        .build()

                    Log.d(TAG, "در حال ارسال ویس به سرور...")
                    val resp = httpClient.newCall(request).execute()

                    if (!resp.isSuccessful) {
                        throw Exception("خطای سرور (${resp.code})")
                    }

                    val body = resp.body.string()
                    JSONObject(body)
                }

                if (response.has("error")) {
                    Log.e(TAG, "server response: $response")
                    throw Exception(response.getString("error"))
                }

                val replyText = response.optString("reply", "")
                val replyVoiceUrl = response.optString("reply_voice", "")

                deleteMessage(typingMessage)

                if (replyVoiceUrl.isNotBlank()) {
                    val voiceUri = "$BASE_URL/$replyVoiceUrl".toUri()
                    val (calcDuration, calcAmplitudes) = calculateAudioData(voiceUri)
                    val aiVoiceMessage = ChatMessage.VoiceMessage(
                        uri = voiceUri,
                        duration = calcDuration,
                        amplitudes = calcAmplitudes,
                        fromUser = false
                    )
                    addMessage(aiVoiceMessage)
                }

                if (replyText.isNotBlank()) {
                    val aiTextMessage = ChatMessage.TextMessage(
                        text = replyText,
                        fromUser = false
                    )
                    addMessage(aiTextMessage)
                }

                if (replyVoiceUrl.isBlank() && replyText.isBlank()) {
                    val fallbackMessage = ChatMessage.TextMessage(
                        text = "پاسخی از سمت هوش مصنوعی دریافت نشد.",
                        fromUser = false
                    )
                    addMessage(fallbackMessage)
                }

            } catch (e: Exception) {
                Log.e(TAG, "خطا در ارسال ویس", e)
                _messages.value = _messages.value.map {
                    if (it.id == typingMessage.id)
                        ChatMessage.TextMessage(
                            text = "❌ خطا در ارسال صدا: ${e.message}",
                            fromUser = false
                        )
                    else it
                }
            } finally {
                _isSending.value = false
            }
        }
    }

    // ------------------- FILE HANDLER -------------------
    fun sendFileMessage(
        uri: Uri,
        caption: String?
    ) {
        viewModelScope.launch {
            _isSending.value = true

            val fileInfo = getFileInfo(uri)

            val userFileMessage = ChatMessage.FileMessage(
                uri = uri,
                caption = caption?.takeIf { it.isNotBlank() },
                isImage = fileInfo.isImage,
                fromUser = true
            )
            addMessage(userFileMessage)

            val typingText =
                if (fileInfo.isImage) "در حال پردازش تصویر..." else "در حال پردازش فایل..."
            val typingMessage = ChatMessage.TextMessage(
                text = typingText,
                fromUser = false,
                isTyping = true
            )
            addMessage(typingMessage)

            try {
                val response = withContext(Dispatchers.IO) {
                    val context = getApplication<Application>()
                    var file = uriToTempFileGeneral(context, uri, fileInfo)

                    file = compressImageFile(file)

                    try {
                        val mimeType = fileInfo.mimeType ?: "application/octet-stream"
                        Log.d(
                            TAG,
                            "File: ${file.name}, size: ${file.length()} bytes, type: $mimeType"
                        )

                        val mediaType =
                            mimeType.toMediaTypeOrNull() ?: "application/octet-stream".toMediaType()

                        val requestBuilder = MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart(
                                name = "file",
                                filename = file.name,
                                body = file.asRequestBody(mediaType)
                            )

                        caption?.let {
                            requestBuilder.addFormDataPart("caption", it)
                        }

                        val request = Request.Builder()
                            .url(FILE_API_URL)
                            .post(requestBuilder.build())
                            .build()

                        Log.d(TAG, "Sending file to API...")
                        val resp = httpClient.newCall(request).execute()

                        if (!resp.isSuccessful) {
                            Log.e(TAG, "File API error: ${resp.code}")
                            throw Exception("خطای سرور (کد: ${resp.code})")
                        }

                        val body = resp.body.string()
                        Log.d(TAG, "File API response: $body")

                        JSONObject(body)

                    } finally {
                        if (file.exists()) {
                            val deleted = file.delete()
                            Log.d(TAG, "Temp file deleted: $deleted")
                        }
                    }
                }

                deleteMessage(typingMessage)

                if (response.has("error")) {
                    throw Exception(response.getString("error"))
                }

                val replyText = response.optString("reply", "")
                val replyFile = response.optString("reply_file", "")

                Log.d(TAG, "Reply text: $replyText")
                Log.d(TAG, "Reply file: $replyFile")

                if (replyText.isNotBlank()) {
                    addMessage(
                        ChatMessage.TextMessage(
                            text = replyText,
                            fromUser = false
                        )
                    )
                }

                if (replyFile.isNotBlank()) {
                    val fileUri = "$BASE_URL/$replyFile".toUri()
                    val replyFileInfo = getFileInfo(fileUri)

                    addMessage(
                        ChatMessage.FileMessage(
                            uri = fileUri,
                            caption = null,
                            isImage = replyFileInfo.isImage,
                            fromUser = false
                        )
                    )
                }

                if (replyText.isBlank() && replyFile.isBlank()) {
                    addMessage(
                        ChatMessage.TextMessage(
                            text = "فایل دریافت شد اما پاسخی از سمت هوش مصنوعی دریافت نشد.",
                            fromUser = false
                        )
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "sendFileMessage error", e)

                _messages.update { currentList ->
                    currentList.map {
                        if (it.id == typingMessage.id) {
                            ChatMessage.TextMessage(
                                text = "❌ خطا در ارسال فایل: ${e.localizedMessage ?: e.message}",
                                fromUser = false
                            )
                        } else {
                            it
                        }
                    }
                }
            } finally {
                _isSending.value = false
            }
        }
    }

    private fun uriToTempFileGeneral(context: Context, uri: Uri, fileInfo: FileInfo): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("نمی‌توان فایل را باز کرد")

        val safeExtension =
            if (fileInfo.extension.matches(Regex("[a-zA-Z0-9]+")))
                ".${fileInfo.extension}"
            else
                ".dat"

        val tempFile = File.createTempFile("file_", safeExtension, context.cacheDir)

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    // ------------------- HELPER FUNCTIONS -------------------

    private fun normalizeAndDownsampleAmplitudes(rawAmps: List<Float>): List<Float> {
        if (rawAmps.isEmpty()) return List(40) { 0.05f }

        val chunkSize = 1.coerceAtLeast(rawAmps.size / 40)
        val downsampled = rawAmps.chunked(chunkSize).take(40).map { chunk ->
            chunk.average().toFloat()
        }

        val maxAmp = downsampled.maxOrNull() ?: 1f
        val safeMax = if (maxAmp > 0f) maxAmp else 1f

        return downsampled.map { amp ->
            val normalized = amp / safeMax
            normalized.coerceIn(0.05f, 1f)
        }
    }

    private suspend fun calculateAudioData(uri: Uri): Pair<Long, List<Float>> {
        return withContext(Dispatchers.IO) {
            var duration = 0L

            try {
                val context = getApplication<Application>()

                val tempFile = downloadAudioFile(context, uri)

                if (tempFile != null && tempFile.exists()) {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(tempFile.absolutePath)

                    duration = retriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_DURATION
                    )?.toLongOrNull() ?: 0L

                    retriever.release()
                    tempFile.delete()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error calculating audio duration", e)
            }

            val amplitudes = generateNaturalWaveform()

            Pair(duration, amplitudes)
        }
    }

    private fun generateNaturalWaveform(sampleCount: Int = 50): List<Float> {
        val amplitudes = mutableListOf<Float>()
        val random = Random()

        var phase = 0.0
        val frequency = 0.3 + random.nextDouble() * 0.4

        (0 until sampleCount).forEach { i ->
            val sineWave = sin(phase) * 0.4
            val noise = (random.nextDouble() - 0.5) * 0.3
            val baseAmplitude = 0.4 + sineWave + noise

            val amplitude = baseAmplitude.coerceIn(0.2, 1.0).toFloat()
            amplitudes.add(amplitude)

            phase += frequency + (random.nextDouble() - 0.5) * 0.1
        }

        return amplitudes
    }

    private fun downloadAudioFile(context: Context, uri: Uri): File? {
        return try {
            val url = uri.toString()
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val tempFile = File.createTempFile("audio_", ".mp3", context.cacheDir)
                response.body.byteStream().use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading audio file", e)
            null
        }
    }

    fun clearMessages() {
        if (_messages.value.isNotEmpty()) _messages.value = emptyList()
    }

    fun deleteMessage(message: ChatMessage) {
        _messages.value = _messages.value.filter { it != message }
    }

    override fun onCleared() {
        super.onCleared()
        // اینجا هم اسم متغیر آپدیت شد
        activeNetworkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
        }
        Log.d(TAG, "ViewModel cleared")
    }

    data class FileInfo(
        val isImage: Boolean,
        val mimeType: String?,
        val extension: String
    )

    fun getFileInfo(uri: Uri): FileInfo {
        val context = getApplication<Application>()
        val mimeType = context.contentResolver.getType(uri)

        val extension = when {
            mimeType == null -> ""
            mimeType.contains("pdf") -> "pdf"
            mimeType.contains("word") -> "docx"
            mimeType.contains("excel") || mimeType.contains("spreadsheet") -> "xlsx"
            mimeType.contains("image/jpeg") -> "jpg"
            mimeType.contains("image/png") -> "png"
            mimeType.startsWith("image/") -> "jpg"
            mimeType.startsWith("audio/") -> "mp3"
            mimeType.startsWith("video/") -> "mp4"
            else -> ""
        }

        val isImage = mimeType?.startsWith("image/") == true

        return FileInfo(
            isImage = isImage,
            mimeType = mimeType,
            extension = extension
        )
    }

    private fun compressImageFile(originalFile: File): File {
        try {
            val mimeType = originalFile.name.lowercase()
            if (!mimeType.endsWith(".jpg") && !mimeType.endsWith(".jpeg") && !mimeType.endsWith(".png")) {
                return originalFile
            }

            val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath) ?: return originalFile

            val compressedFile = File(originalFile.parent, "compressed_${originalFile.name}")
            val outStream = FileOutputStream(compressedFile)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outStream)

            outStream.flush()
            outStream.close()

            originalFile.delete()

            return compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            return originalFile
        }
    }
}