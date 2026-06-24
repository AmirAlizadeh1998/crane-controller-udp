package com.tivanco.hymaco.appUI.supportScreen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.tivanco.hymaco.R
import com.tivanco.hymaco.dataClass.VoiceUiState
import com.tivanco.hymaco.utils.fileHandler.FilePickerDialog
import com.tivanco.hymaco.utils.fileHandler.Utils
import com.tivanco.hymaco.utils.voiceHandler.VoiceWaveform
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.log10

@Composable
fun SupportBottomBar(
    focusManager: FocusManager,
    voiceState: VoiceUiState, // استیت رو از ویومدل می‌گیریم
    onStartVoiceRecord: () -> Unit, // متصل به viewModel.startRecording
    onStopVoiceRecord: () -> Unit,  // متصل به viewModel.stopRecording
    onClearVoiceRecord: () -> Unit, // متصل به viewModel.clearVoiceState
    onSendText: (String) -> Unit,
    onSendVoice: (Uri, List<Float>, Long) -> Unit,
    onSendFileWithCaption: (Uri, String) -> Unit
) {
    val context = LocalContext.current

    var text by remember { mutableStateOf("") }

    // مدیریت وضعیت UI ویس بر اساس ویومدل
    val isRecording = voiceState.isRecording
    // اگه ضبط متوقف شده ولی فایلی برای ارسال وجود داره یعنی کاربر ویس رو گرفته و آماده ارساله
    val hasRecorded = !isRecording && voiceState.filePath != null
    val latestVoiceState by rememberUpdatedState(voiceState)

    var durationSec by remember { mutableIntStateOf(0) }
    val waveform = remember { mutableStateListOf<Float>().apply { addAll(List(30) { 0.05f }) } }
    val recordedAmps = remember { mutableStateListOf<Float>() }

    var showFileDialog by remember { mutableStateOf(false) }
    var pendingFileUri by remember { mutableStateOf<Uri?>(null) }
    var fileCaption by remember { mutableStateOf("") }

    // ---------------- FILE PICKER ----------------
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { pendingFileUri = it }
    }

    // ---------------- CAMERA ----------------
    val photoUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri.value?.let { pendingFileUri = it }
        }
    }

    fun launchCamera() {
        val uri = Utils.createImageUri(context)
        photoUri.value = uri
        cameraLauncher.launch(uri)
    }

    // ---------------- RECORDING LOOP ----------------
    LaunchedEffect(isRecording) {
        if (!isRecording) return@LaunchedEffect

        val startTime = System.currentTimeMillis()
        durationSec = 0
        recordedAmps.clear()
        waveform.clear()
        // واتس‌اپ معمولا میله‌های بیشتری نشون میده (مثلا 40 تا)
        waveform.addAll(List(40) { 0.05f })

        var smoothedAmp = 0.05f // متغیر برای نرم کردن حرکت میله‌ها

        while (true) {
            // خواندن مقدار از latestVoiceState به جای voiceState مستقیم
            val rawAmp = latestVoiceState.amplitude.toFloat()

            // محاسبه مقدار هدف (Target) با همون فرمول لگاریتمی
            val targetAmp = if (rawAmp > 10f) {
                (log10(rawAmp) / 4.5f).coerceIn(0.05f, 1f)
            } else {
                0.05f
            }

            Log.d("VoiceWaveform", "Raw Amplitude: $rawAmp")
            // 🚀 جادوی واتس‌اپ: حرکت نرم (Interpolation)
            // به جای اینکه یهو بپریم رو عدد جدید، 30 درصد 30 درصد بهش نزدیک میشیم
            smoothedAmp += (targetAmp - smoothedAmp) * 0.3f

            // اطمینان از اینکه از محدوده‌ها خارج نمیشه
            val finalAmp = smoothedAmp.coerceIn(0.05f, 1f)

            waveform.removeAt(0)
            waveform.add(finalAmp)
            recordedAmps.add(finalAmp)

            durationSec = ((System.currentTimeMillis() - startTime) / 1000).toInt()

            // تاخیر 40 میلی‌ثانیه برای روانی انیمیشن (مثل واتس‌اپ)
            delay(40)
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onStartVoiceRecord()
        } else {
            Toast.makeText(context, "برای ارسال ویس به دسترسی میکروفون نیاز است", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- UI ----------------
    // ---------------- UI ----------------
    if (pendingFileUri != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF2F2F2)) // هم‌رنگ با نوار حالت عادی
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // دکمه انصراف (حذف فایل)
            IconButton(
                onClick = {
                    pendingFileUri = null
                    fileCaption = ""
                    focusManager.clearFocus() // بستن کیبورد
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.delete),
                    contentDescription = "Cancel",
                    tint = Color(0xFFB33A3A) // قرمز رنگ برای حذف
                )
            }

            // فیلد متنی کپشن با استایل یکپارچه
            TextField(
                value = fileCaption,
                onValueChange = { fileCaption = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                placeholder = { Text("کپشن (اختیاری)…") },
                shape = RoundedCornerShape(25.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                leadingIcon = {
                    // یه نشونه که کاربر بدونه فایلی اتچ شده
                    Icon(
                        painter = painterResource(R.drawable.attach),
                        contentDescription = "Attached",
                        tint = Color(0xFF412473)
                    )
                }
            )

            // دکمه ارسال شبیه حالت عادی
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .background(Color(0xFF412473), shape = CircleShape)
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = {
                        onSendFileWithCaption(pendingFileUri!!, fileCaption.trim())
                        pendingFileUri = null
                        fileCaption = ""
                        focusManager.clearFocus() // بستن کیبورد بعد از ارسال
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.up),
                        contentDescription = "Send file",
                        tint = Color.White
                    )
                }
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF2F2F2))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SEND BUTTON
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .background(Color(0xFF412473), shape = CircleShape)
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    modifier = Modifier.size(24.dp),
                    enabled = !isRecording,
                    onClick = {
                        when {
                            hasRecorded -> {
                                val filePath = voiceState.filePath
                                if (recordedAmps.isNotEmpty()) {
                                    val fileUri = Uri.fromFile(File(filePath))

                                    onSendVoice(fileUri, recordedAmps.toList(), durationSec * 1000L)
                                }

                                // فقط استیت‌های محلی UI رو ریست می‌کنیم که برگرده به حالت تایپ
                                waveform.clear()
                                waveform.addAll(List(30) { 0.05f })
                                recordedAmps.clear()
                                durationSec = 0
                                onClearVoiceRecord() // حتما استیت ویومدل هم ریست بشه
                            }

                            text.isNotBlank() -> {
                                onSendText(text.trim())
                                text = ""
                                focusManager.clearFocus()
                            }
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.up),
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }

            // TEXT / WAVEFORM
            if (isRecording || hasRecorded) {
                VoiceWaveform(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    amplitudes = waveform.toList(),
                    durationSeconds = durationSec,
                    progressSeconds = durationSec.toFloat()
                )

                Text(
                    text = formatSeconds(durationSec),
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("پیام خود را بنویسید…") },
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(25.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            // RECORD BUTTON
            IconButton(
                onClick = {
                    when {
                        !isRecording && !hasRecorded -> {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) onStartVoiceRecord()
                            else recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                        isRecording -> onStopVoiceRecord()
                        else -> {
                            onClearVoiceRecord()
                            waveform.clear()
                            waveform.addAll(List(30) { 0.05f })
                            recordedAmps.clear()
                            durationSec = 0
                        }
                    }
                }
            ) {
                Icon(
                    painter = painterResource(
                        when {
                            isRecording -> R.drawable.stop_record
                            hasRecorded -> R.drawable.delete
                            else -> R.drawable.microphone
                        }
                    ),
                    contentDescription = "Voice",
                    tint = when {
                        isRecording || hasRecorded -> Color(0xFFB33A3A)
                        else -> Color(0xFF5B7C99)
                    }
                )
            }

            // ATTACH BUTTON
            IconButton(
                enabled = !isRecording && !hasRecorded,
                onClick = { showFileDialog = true }
            ) {
                Icon(
                    painter = painterResource(R.drawable.attach),
                    contentDescription = "Attach",
                    tint = Color(0xFF5B7C99)
                )
            }
        }
    }

    // ---------------- ATTACH DIALOG ----------------
    if (showFileDialog) {
        FilePickerDialog(
            onDismiss = { showFileDialog = false },
            onCamera = {
                launchCamera()
                showFileDialog = false
            },
            onGallery = {
                fileLauncher.launch("*/*")
                showFileDialog = false
            }
        )
    }
}