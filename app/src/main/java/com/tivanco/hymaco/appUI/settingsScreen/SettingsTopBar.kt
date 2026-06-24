package com.tivanco.hymaco.appUI.settingsScreen

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.tivanco.hymaco.R
import com.tivanco.hymaco.customViews.AppTopBar

@Composable
fun SettingsTopBar(
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    AppTopBar(
        title = "تنظیمات",
        actionIcons = {
            IconButton(
                onClick = { onSaveClick() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.save),
                    contentDescription = "Save",
                    tint = Color(0xffe3e3e3)
                )
            }
        },
        navigationIcons = {
            IconButton(
                onClick = {
                    onBackClick()
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "Back",
                    tint = Color(0xffe3e3e3)
                )
            }
        }
    )
}