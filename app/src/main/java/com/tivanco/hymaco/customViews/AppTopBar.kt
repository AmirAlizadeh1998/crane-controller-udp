package com.tivanco.hymaco.customViews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcons: @Composable (() -> Unit)? = null,
    actionIcons: @Composable (() -> Unit)? = null
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Surface(
            color = Color(0xff1E3A8A),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = modifier
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🔹 آیکون‌های سمت چپ (مثلاً برگشت یا منو)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    navigationIcons?.invoke()
                }

                // 🔹 عنوان وسط
                Text(
                    text = title,
                    fontSize = 20.sp,
                    color = Color(0xFFF3F3F3),
                    style = TextStyle(textDirection = TextDirection.Rtl),
                    modifier = Modifier.padding(end = 8.dp)
                )

                // 🔹 آیکون‌های سمت راست (مثلاً جستجو، پروفایل، تنظیمات)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    actionIcons?.invoke()
                }
            }
        }
    }
}