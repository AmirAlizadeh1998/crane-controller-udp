package com.tivanco.hymaco.utils.voiceHandler

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import java.io.File

object VoiceRecorder {

    var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun startRecording(context: Context) {
        try {
            releaseRecorder()

            outputFile = File(
                context.getExternalFilesDir(null),
                "voice_${System.currentTimeMillis()}.m4a"
            )

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile!!.absolutePath)

                prepare()
                start()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            releaseRecorder()
            outputFile = null
        }
    }

    fun stopRecording(): Uri? {
        return try {
            recorder?.apply {
                stop()    // اگر فایل هست stop می‌زند
                release()
            }
            recorder = null

            outputFile?.let { Uri.fromFile(it) }

        } catch (_: Exception) {
            null
        }
    }

    private fun releaseRecorder() {
        try {
            recorder?.reset()
            recorder?.release()
        } catch (_: Exception) { }
        recorder = null
    }
}