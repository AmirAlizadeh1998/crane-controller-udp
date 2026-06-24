package com.tivanco.hymaco.viewModel

import androidx.lifecycle.ViewModel
import com.tivanco.hymaco.repository.UdpRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainUiViewModel(repository: UdpRepository) : ViewModel() {
    val connectionEvent = repository.connectionEvent
    private val _btnReady = MutableStateFlow(false)
    val btnReady = _btnReady.asStateFlow()
    private val _clutchPressing = MutableStateFlow(false)
    private val _isClutchPressed = MutableStateFlow(false)
    var isClutchPressed = _isClutchPressed.asStateFlow()

    fun setClutchState(value: Boolean) {
        _isClutchPressed.value = value
    }

    fun setBtnReady(value: Boolean) {
        _btnReady.value = value
    }

    fun setClutchPressing(value: Boolean) {
        _clutchPressing.value = value
    }
}

