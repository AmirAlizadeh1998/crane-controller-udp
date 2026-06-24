package com.tivanco.hymaco.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tivanco.hymaco.repository.UdpRepository
import kotlinx.coroutines.launch

class LogAndErrorViewModel(
    private val repository: UdpRepository
    // private val databaseRepository: DatabaseRepository // اگه روم داری
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.receivedMessages.collect { message ->
                // بررسی می‌کنیم اگه پیام از نوع ارور بود، ذخیرش کنیم
                if (message.startsWith("ERROR_")) {
                    saveErrorToDatabase(message)
                }
                // یا مثلا لاگ‌های عادی رو برای تب دیباگ ذخیره کنیم
            }
        }
    }

    private fun saveErrorToDatabase(errorMsg: String) {
        // لاجیک ذخیره در Room
    }
}
