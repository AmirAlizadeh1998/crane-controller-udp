package com.tivanco.hymaco.appUI.mainScreen

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tivanco.hymaco.customViews.PushButton
import com.tivanco.hymaco.viewModel.MainUiViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeBottomBar(
    uiViewModel: MainUiViewModel,
    onClutchPress: () -> Unit,
    onClutchRelease: () -> Unit
) {
    val btnReadyVm by uiViewModel.btnReady.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        PushButton(
            uiViewModel = uiViewModel,
            text = "کلاچ",
            onPress = { onClutchPress(); Log.i("PushButton", "Pressed") },
            onRelease = { onClutchRelease(); Log.i("PushButton", "Released") },
            enabled = btnReadyVm,
        )
    }
}