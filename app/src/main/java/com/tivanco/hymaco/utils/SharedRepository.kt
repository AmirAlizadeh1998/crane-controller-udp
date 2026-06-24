package com.tivanco.hymaco.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SharedRepository {
    private val _debugMsg = MutableStateFlow("")
    val debugMsg: StateFlow<String> = _debugMsg.asStateFlow()

    fun updateDebugMsg(newValue: String) {
        _debugMsg.value = newValue
    }
}
