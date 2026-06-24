package com.tivanco.hymaco.appUI.supportScreen

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tivanco.hymaco.R
import com.tivanco.hymaco.customViews.AppTopBar
import com.tivanco.hymaco.customViews.CustomAppDialog
import com.tivanco.hymaco.utils.exit
import com.tivanco.hymaco.viewModel.ai.SupportViewModel

@Composable
fun SupportScreen(
    focusManager: FocusManager,
    navController: NavHostController
) {
    val viewModel: SupportViewModel = viewModel()
    val messages = viewModel.messages.collectAsState().value
    val voiceState by viewModel.voiceState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val bottomPadding =
        10.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.padding(bottom = bottomPadding),
            containerColor = Color(0xFFF5F5F5),

            topBar = {
                AppTopBar(
                    title = "پشتیبانی",
                    navigationIcons = {
                        IconButton(onClick = { exit(navController) }) {
                            Icon(
                                painter = painterResource(R.drawable.back),
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actionIcons = {
                        IconButton(onClick = {
                            if (messages.isNotEmpty()) showDeleteDialog = true
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.delete),
                                contentDescription = "clear all messages",
                                tint = Color.White
                            )
                        }
                    }
                )
            },

            bottomBar = {
                SupportBottomBar(
                    focusManager = focusManager,
                    voiceState = voiceState,
                    onStartVoiceRecord = { viewModel.startRecording() },
                    onStopVoiceRecord = { viewModel.stopRecording() },
                    onClearVoiceRecord = { viewModel.clearVoiceState() },
                    onSendText = { text ->
                        viewModel.sendTextMessage(text)
                    },
                    onSendVoice = { uri, amps, duration ->
                        viewModel.sendVoiceMessage(uri, amps, duration)
                    },
                    onSendFileWithCaption = { uri, caption ->
                        viewModel.sendFileMessage(uri, caption)
                    }
                )
            }
        ) { innerPadding ->
            Support(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                viewModel = viewModel
            )
        }

        if (showDeleteDialog) {
            CustomAppDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = "پاک کردن چت",
                description = "همه پیام ها پاک خواهند شد.این عمل قابل برگشت نیست",
                onConfirm = {
                    viewModel.clearMessages()
                    showDeleteDialog = false
                }
            )
        }
    }
}