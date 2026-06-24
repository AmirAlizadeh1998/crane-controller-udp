package com.tivanco.hymaco.customViews

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
fun LeftRightButton(
    modifier: Modifier = Modifier,
    text: String,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
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
        Row(
            modifier = modifier
                .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(10.dp))
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArrowButton(
                iconResource = R.drawable.arrow_right,
                desc = "Arrow right",
                onPress = { onRightClick() },
                onRelease = { onRelease() },
                enabled = enabled
            )
            Text(
                text = text,
                color = Color.Black,
                fontSize = responsiveSizes().fontSize
            )
            ArrowButton(
                iconResource = R.drawable.arrow_left,
                desc = "Arrow left",
                onPress = { onLeftClick() },
                onRelease = { onRelease() },
                enabled = enabled
            )
        }
    }
}