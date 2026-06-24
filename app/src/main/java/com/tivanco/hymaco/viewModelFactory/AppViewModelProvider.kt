package com.tivanco.hymaco.viewModelFactory

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tivanco.hymaco.activityAndApplication.CraneApplication
import com.tivanco.hymaco.viewModel.ConnectionViewModel
import com.tivanco.hymaco.viewModel.CraneControlViewModel
import com.tivanco.hymaco.viewModel.CraneLogViewModel
import com.tivanco.hymaco.viewModel.LogAndErrorViewModel
import com.tivanco.hymaco.viewModel.MainUiViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application =
                (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CraneApplication)
            val container = application.container

            ConnectionViewModel(container.udpRepository)
        }

        initializer {
            val application =
                (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CraneApplication)
            val container = application.container

            CraneControlViewModel(container.udpRepository, container.parser)
        }

        initializer {
            val application =
                (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CraneApplication)
            val container = application.container

            LogAndErrorViewModel(container.udpRepository)
        }

        initializer {
            val application =
                (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CraneApplication)
            val container = application.container

            CraneLogViewModel(container.udpRepository, container.appLogDao, container.sysLogDao)
        }

        initializer {
            val application =
                (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CraneApplication)
            val container = application.container

            MainUiViewModel(container.udpRepository)
        }
    }
}