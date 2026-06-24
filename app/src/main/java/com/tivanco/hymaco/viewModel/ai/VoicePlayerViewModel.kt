package com.tivanco.hymaco.viewModel.ai

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tivanco.hymaco.utils.voiceHandler.VoicePlayerController
import com.tivanco.hymaco.utils.voiceHandler.VoicePlayerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VoicePlayerViewModel : ViewModel(), VoicePlayerController {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    var isVoicePlaying = mutableStateOf(false)
        private set

    var progressMs = mutableIntStateOf(0)
        private set

    var durationMs = mutableIntStateOf(0)
        private set

    var isCompleted = mutableStateOf(false)
        private set

    private var currentUri: Uri? = null
    private var pendingPlay = false

    init {
        VoicePlayerManager.register(this)
    }


    fun toggle(uri: Uri, context: Context) {

        if (currentUri == null || currentUri != uri) {
            releasePlayer()
            preparePlayer(uri, context)
        }

        if (isVoicePlaying.value) {
            pause()
        } else {
            VoicePlayerManager.onPlayRequested(this)
            pendingPlay = true
            startIfReady()
        }
    }


    private fun preparePlayer(uri: Uri, context: Context) {

        currentUri = uri

        mediaPlayer = MediaPlayer().apply {

            setOnCompletionListener {
                handleCompletion()
            }

            setOnPreparedListener { mp ->
                durationMs.intValue = mp.duration
                startIfReady()
            }

            setDataSource(context, uri)
            prepareAsync()
        }
    }

    private fun startIfReady() {
        if (!pendingPlay) return
        if (durationMs.intValue == 0) return

        play()
        pendingPlay = false
    }

    private fun play() {
        mediaPlayer?.start()
        isVoicePlaying.value = true
        isCompleted.value = false
        startProgressJob()
    }

    private fun pause() {
        mediaPlayer?.pause()
        isVoicePlaying.value = false
        progressJob?.cancel()
        progressJob = null
    }

    private fun handleCompletion() {
        progressJob?.cancel()
        progressJob = null

        isVoicePlaying.value = false
        isCompleted.value = true
        progressMs.intValue = 0

        mediaPlayer?.release()
        mediaPlayer = null

        // ⬅⬅⬅ مهم‌ترین خط برای رفع مشکل
        currentUri = null
    }

    private fun startProgressJob() {
        progressJob?.cancel()

        progressJob = viewModelScope.launch {

            while (isVoicePlaying.value && mediaPlayer != null) {

                val p = mediaPlayer ?: break
                val pos = p.currentPosition
                val dur = p.duration

                progressMs.intValue = pos

                if (dur > 0 && pos >= dur - 30) {
                    handleCompletion()
                    break
                }

                delay(50)
            }
        }
    }


    private fun releasePlayer() {
        progressJob?.cancel()
        progressJob = null

        mediaPlayer?.release()
        mediaPlayer = null

        isVoicePlaying.value = false
        isCompleted.value = false
        pendingPlay = false

        progressMs.intValue = 0
        durationMs.intValue = 0
    }


    override fun onCleared() {
        super.onCleared()
        VoicePlayerManager.unregister(this)
        releasePlayer()
    }


    override fun stopPlaying() {
        handleCompletion()
    }
}