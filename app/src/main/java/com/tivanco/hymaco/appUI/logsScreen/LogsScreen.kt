package com.tivanco.hymaco.appUI.logsScreen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tivanco.hymaco.R
import com.tivanco.hymaco.customViews.AppTopBar
import com.tivanco.hymaco.utils.exit
import com.tivanco.hymaco.viewModel.CraneControlViewModel

@Composable
fun LogsScreen(
    navController: NavHostController,
    craneControlViewModel: CraneControlViewModel
) {
    val bottomPadding =
        10.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    DisposableEffect(Unit) {

        // کاربر وارد شد: مود استاتوس رو روشن کن
        craneControlViewModel.setLogMode(true)

        // کاربر رفت: برگرد به مود کنترل اصلی
        onDispose {
            craneControlViewModel.setLogMode(false)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.padding(bottom = bottomPadding),
            containerColor = Color(0xFFF5F5F5),

            topBar = {
                AppTopBar(
                    title = "گزارش",
                    navigationIcons = {
                        IconButton(onClick = { exit(navController) }) {
                            Icon(
                                painter = painterResource(R.drawable.back),
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                )
            },

//            bottomBar = {
//                SupportBottomBar(
//                    focusManager = focusManager,
//                    onSendText = { text ->
//                        viewModel.sendTextMessage(text)
//                    },
//                    onSendVoice = { uri, amplitudes, duration ->
//                        viewModel.sendVoiceMessage(uri, amplitudes, duration)
//                    },
//                    onSendFileWithCaption = { uri, caption ->
//                        viewModel.sendFileMessage(uri, caption)
//                    }
//                )
//            }
        ) { innerPadding ->
            Logs(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                craneControlViewModel = craneControlViewModel
            )
        }
    }
}