package com.tivanco.hymaco.customViews.customToast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.tivanco.hymaco.utils.responsiveSizes
import kotlinx.coroutines.delay

@Composable
fun CustomToast(
    key: Any,
    message: String,
    iconRes: Int? = null,
    backgroundColor: Color = Color.Black,
    contentColor: Color = Color.White,
    duration: Long = 2000L,
    onDismiss: () -> Unit
) {
    var visible by remember(key) { mutableStateOf(false) }

    LaunchedEffect(key) {
        visible = true
        delay(duration)
        visible = false
        delay(300)
        onDismiss()
    }

    // باکس اصلی که کل صفحه رو می‌گیره (بدون انیمیشن، فقط برای موقعیت‌دهی)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp), // توست معمولا بالاست، اگه پایین میخوای padding bottom بده
        contentAlignment = Alignment.TopCenter
    ) {
        // انیمیشن فقط روی خود کادر توست اعمال میشه
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it }
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .background(backgroundColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    color = contentColor,
                    fontSize = responsiveSizes().toastFontSize,
                    fontWeight = FontWeight.Medium,
                    style = TextStyle(textDirection = TextDirection.Rtl)
                )
                iconRes?.let {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}