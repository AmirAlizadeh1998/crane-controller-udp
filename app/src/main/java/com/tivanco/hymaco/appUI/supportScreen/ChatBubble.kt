package com.tivanco.hymaco.appUI.supportScreen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.tivanco.hymaco.R
import com.tivanco.hymaco.dataClass.ChatMessage
import com.tivanco.hymaco.utils.voiceHandler.VoiceWaveform
import com.tivanco.hymaco.viewModel.ai.VoicePlayerViewModel

@Composable
fun ChatBubble(
    message: ChatMessage
) {
    val bubbleColor = if (message.fromUser) Color(0xFFD6E4FF) else Color(0xFFE3F8E6)
    val align = if (message.fromUser) Arrangement.Start else Arrangement.End
    val context = LocalContext.current

    Row(
        horizontalArrangement = align,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .padding(6.dp)
                .widthIn(max = 250.dp)
        ) {
            when (message) {
                is ChatMessage.TextMessage -> {
                    Text(
                        text = message.text,
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Rtl),
                        modifier = Modifier.padding(10.dp),
                        fontSize = 16.sp,
                    )
                }

                is ChatMessage.FileMessage -> {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // نمایش پیش‌نمایش
                        if (message.isImage) {
                            // پیش‌نمایش تصویر
                            AsyncImage(
                                model = message.uri,
                                contentDescription = "تصویر ارسالی",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { openFile(message.uri, context) },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // پیش‌نمایش فایل (آیکون + نام)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color.White.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                                    .clickable{ openFile(message.uri, context) },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // آیکون فایل
                                Icon(
                                    painter = painterResource(getFileIcon(message.uri)),
                                    contentDescription = null,
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(40.dp)
                                )

                                // اطلاعات فایل
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = getFileName(message.uri),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = getFileSize(message.uri),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // کپشن (اگر وجود داشته باشد)
                        message.caption?.let { caption ->
                            Text(
                                text = caption,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDirection = TextDirection.Rtl
                                ),
                                color = Color.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                is ChatMessage.VoiceMessage -> {
                    val context = LocalContext.current
                    val uri = message.uri ?: return@Card  // ← اگر null است، نمایش نمی‌دهیم
                    val player = viewModel<VoicePlayerViewModel>(key = uri.toString())

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        IconButton(
                            onClick = { player.toggle(uri, context) }
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (player.isVoicePlaying.value) R.drawable.pause else R.drawable.play_icon
                                ),
                                contentDescription = "",
                                tint = Color.Black
                            )
                        }

                        if (message.amplitudes.isNotEmpty()) {
                            val durationSec = (message.duration / 1000).toInt()
                            val progressSec = player.progressMs.intValue / 1000f

                            VoiceWaveform(
                                amplitudes = message.amplitudes,
                                durationSeconds = durationSec,
                                progressSeconds = progressSec,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            )

                            Text(
                                text = formatSeconds(progressSec.toInt()),
                                color = Color.Black,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatSeconds(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return "%02d:%02d".format(m, s)
}

// در بالای فایل یا در یک Helper file
private fun getFileName(uri: Uri): String {
    val path = uri.path ?: return "فایل"
    return path.substringAfterLast('/').ifEmpty { "فایل" }
}

@Composable
private fun getFileSize(uri: Uri): String {
    val context = LocalContext.current
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val bytes = stream.available().toLong()
            when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                else -> "${bytes / (1024 * 1024)} MB"
            }
        } ?: "نامشخص"
    } catch (_: Exception) {
        "نامشخص"
    }
}

@Composable
private fun getFileIcon(uri: Uri): Int {
    val context = LocalContext.current

    // ابتدا MIME type رو چک کن
    val mimeType = context.contentResolver.getType(uri)

    if (mimeType != null) {
        return when {
            mimeType.startsWith("image/") -> R.drawable.image
            mimeType.startsWith("video/") -> R.drawable.video
            mimeType.startsWith("audio/") -> R.drawable.audio
            mimeType == "application/pdf" -> R.drawable.pdf

            // Excel - باید قبل از Word چک بشه
            mimeType.contains("spreadsheet") ||
                    mimeType.contains("excel") ||
                    mimeType == "application/vnd.ms-excel" ||
                    mimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> R.drawable.excel

            // Word
            mimeType.contains("word") ||
                    mimeType.contains("document") ||
                    mimeType == "application/msword" ||
                    mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> R.drawable.doc

            // فشرده
            mimeType.contains("zip") ||
                    mimeType.contains("rar") ||
                    mimeType.contains("compressed") -> R.drawable.winrar

            else -> R.drawable.file_icon
        }
    }

    // اگر MIME type نبود، پسوند رو چک کن
    val extension = uri.toString().substringAfterLast('.', "").lowercase()
    return when (extension) {
        "jpg", "jpeg", "png", "gif", "webp", "bmp" -> R.drawable.image
        "pdf" -> R.drawable.pdf
        "xls", "xlsx" -> R.drawable.excel
        "doc", "docx" -> R.drawable.doc
        "zip", "rar", "7z" -> R.drawable.winrar
        "mp3", "wav", "m4a", "ogg" -> R.drawable.audio
        "mp4", "avi", "mkv", "mov" -> R.drawable.video
        else -> R.drawable.file_icon
    }
}

private fun openFile(uri: Uri, context: Context) {
    try {
        val mimeType = context.contentResolver.getType(uri) ?: "*/*"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (_: android.content.ActivityNotFoundException) {
            val chooserIntent = Intent.createChooser(intent, "باز کردن با...")
            context.startActivity(chooserIntent)
        }

    } catch (e: Exception) {
        Toast.makeText(context, "خطا در باز کردن فایل", Toast.LENGTH_SHORT).show()
        Log.e("ChatBubble", "Error opening file", e)
    }
}