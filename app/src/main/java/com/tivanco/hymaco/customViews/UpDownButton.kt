package com.tivanco.hymaco.customViews

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tivanco.hymaco.R
import com.tivanco.hymaco.utils.responsiveSizes

@Composable
fun UpDownButton(
    modifier: Modifier = Modifier,
    text: String,
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    onRelease: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .border(width = 1.dp, color = Color(0xFF919191), shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF1))
    ) {
        Column(
            modifier = modifier
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArrowButton(
                onPress = { onUpClick() },
                onRelease = { onRelease() },
                iconResource = R.drawable.arrow_up,
                desc = "Arrow up",
                enabled = enabled
            )
            Text(
                text = text,
                color = Color.Black,
                fontSize = responsiveSizes().fontSize
            )
            ArrowButton(
                onPress = { onDownClick() },
                onRelease = { onRelease() },
                iconResource = R.drawable.arrow_down,
                desc = "Arrow down",
                enabled = enabled
            )
        }
    }
}