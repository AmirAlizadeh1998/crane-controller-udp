package com.tivanco.hymaco.customViews

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tivanco.hymaco.R
import com.tivanco.hymaco.utils.responsiveSizes
import com.tivanco.hymaco.viewModel.MainUiViewModel

@Composable
fun OpenCloseButton(
    text: String,
    ledState: LedState = LedState.OFF,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onRelease: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFECEFF1),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF919191),
                shape = RoundedCornerShape(16.dp)
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegButton(
            icon = R.drawable.arrow_down,
            onPress = { onOpen() },
            onRelease = { onRelease() },
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text,
                    color = Color.Black,
                    fontSize = responsiveSizes().fontSize
                )

                LedIndicator(
                    state = ledState,
                    showLabel = false
                )
            }
        }

        LegButton(
            icon = R.drawable.arrow_up,
            onPress = { onClose() },
            onRelease = { onRelease() },
        )
    }
}