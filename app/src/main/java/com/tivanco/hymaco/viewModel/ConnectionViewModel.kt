package com.tivanco.hymaco.viewModel

import androidx.lifecycle.ViewModel
import com.tivanco.hymaco.repository.UdpRepository

class ConnectionViewModel(
    private val repository: UdpRepository
) : ViewModel() {

    val connectionStatus = repository.connectionStatus
    val statusMessage = repository.statusMessage

    fun startConnection() {
        repository.connect()
    }

    fun stopConnection() {
        repository.disconnect()
    }
}
