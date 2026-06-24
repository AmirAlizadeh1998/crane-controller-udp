package com.tivanco.hymaco.utils.voiceHandler

object VoicePlayerManager {

    private val activePlayers = mutableListOf<VoicePlayerController>()

    fun register(player: VoicePlayerController) {
        activePlayers.add(player)
    }

    fun unregister(player: VoicePlayerController) {
        activePlayers.remove(player)
    }

    fun onPlayRequested(requester: VoicePlayerController) {
        activePlayers.forEach { player ->
            if (player != requester) player.stopPlaying()
        }
    }
}

interface VoicePlayerController {
    fun stopPlaying()
}
