package com.tivanco.hymaco.activityAndApplication

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tivanco.hymaco.appUI.navigation.AppNavigation
import com.tivanco.hymaco.customViews.customToast.LocalToastManager
import com.tivanco.hymaco.customViews.customToast.ToastHost
import com.tivanco.hymaco.customViews.customToast.ToastManager
import com.tivanco.hymaco.ui.theme.TestApplicationTheme
import com.tivanco.hymaco.viewModel.ConnectionViewModel
import com.tivanco.hymaco.viewModel.CraneControlViewModel
import com.tivanco.hymaco.viewModel.CraneLogViewModel
import com.tivanco.hymaco.viewModel.LogAndErrorViewModel
import com.tivanco.hymaco.viewModel.MainUiViewModel
import com.tivanco.hymaco.viewModelFactory.AppViewModelProvider

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val toastManager = remember { ToastManager() }

            val connectionViewModel: ConnectionViewModel =
                viewModel(factory = AppViewModelProvider.Factory)

            val craneControlViewModel: CraneControlViewModel =
                viewModel(factory = AppViewModelProvider.Factory)

            val logAndErrorViewModel: LogAndErrorViewModel =
                viewModel(factory = AppViewModelProvider.Factory)

            val craneLogViewModel: CraneLogViewModel =
                viewModel(factory = AppViewModelProvider.Factory)

            val uiViewModel: MainUiViewModel =
                viewModel(factory = AppViewModelProvider.Factory)

            CompositionLocalProvider(LocalToastManager provides toastManager) {
                TestApplicationTheme {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavigation(
                            connectionViewModel,
                            craneControlViewModel,
                            logAndErrorViewModel,
                            uiViewModel,
                            craneLogViewModel
                        )
                        ToastHost(toastManager)
                    }
                }
            }
        }
    }
}
