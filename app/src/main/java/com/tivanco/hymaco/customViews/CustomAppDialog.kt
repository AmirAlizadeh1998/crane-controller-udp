package com.tivanco.hymaco.customViews

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun CustomAppDialog(
    onDismissRequest: () -> Unit,
    title: String? = null,
    description: String? = null,
    icon: ImageVector? = null,
    confirmButtonText: String = "تأیید",
    dismissButtonText: String? = "انصراف",
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // آیکون (اختیاری)
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = "Dialog Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(bottom = 16.dp)
                    )
                }

                // عنوان (اختیاری)
                title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (description != null || content != null) 8.dp else 24.dp)
                    )
                }

                // متن توضیحات (اختیاری)
                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (content != null) 16.dp else 24.dp)
                    )
                }

                // محتوای کاستوم کاربر (مثل TextField یا عکس و ...)
                if (content != null) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        content()
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // دکمه‌ها
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // دکمه انصراف (اگر متنش null باشه کلا مخفی میشه)
                    dismissButtonText?.let {
                        TextButton(
                            onClick = {
                                onDismiss?.invoke()
                                onDismissRequest() // بستن دیالوگ
                            }
                        ) {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // دکمه تایید
                    Button(
                        onClick = {
                            onConfirm()
                            // اینجا onDismissRequest رو صدا نمی‌زنیم چون شاید کاربر بخواد 
                            // قبل از بسته شدن دیالوگ، دیتا رو ولیدیت (Validate) کنه.
                            // پس بستن دیالوگ رو می‌سپاریم به خود کلاینت.
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = confirmButtonText)
                    }
                }
            }
        }
    }
}
