package com.tivanco.hymaco.appUI

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.tivanco.hymaco.R
import com.tivanco.hymaco.dataClass.LogType
import com.tivanco.hymaco.dataClass.Status
import com.tivanco.hymaco.room.LogSyncWorker
import com.tivanco.hymaco.viewModel.CraneLogViewModel
import kotlinx.coroutines.launch

@Composable
fun CraneLogScreen(viewModel: CraneLogViewModel) {
    val context = LocalContext.current
    // گرفتن هر دو لیست از ویومدل
    val udpLogs by viewModel.logs.collectAsStateWithLifecycle()
    val systemLogs by viewModel.sysLogs.collectAsStateWithLifecycle()
    val host by viewModel.host.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()

    // استیت برای مدیریت تب فعلی
    var showSystemLogs by remember { mutableStateOf(false) }

    val logType = LogType()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // تصمیم‌گیری اینکه کدوم لیست باید الان رندر بشه
    val currentList = if (showSystemLogs) systemLogs else udpLogs

    val isUserAtBottom by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex <= 2
        }
    }

    // آپدیت اسکرول بر اساس اولین آیتم لیستی که الان داره نمایش داده میشه
    LaunchedEffect(currentList.firstOrNull()) {
        if (currentList.isNotEmpty() && isUserAtBottom) {
            listState.scrollToItem(0)
        }
    }

    // وقتی کاربر تب رو عوض میکنه، لیست رو برگردونیم پایین تا گیج نشه
    LaunchedEffect(showSystemLogs) {
        if (currentList.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(8.dp)
    ) {
        // --- بخش هدر ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Crane UDP Terminal",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Status: $connectionStatus | IP: ${if(host == "255.255.255.255") "Broadcast" else host}",
                    color = if (connectionStatus == Status.CONNECTED) Color(0xFF50FA7B) else Color(0xFFFF5555),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (!showSystemLogs) {
                // تو تب UDP دکمه Clear رو نشون بده
                Button(
                    onClick = { viewModel.clearLogs() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Clear", color = Color.White, fontSize = 12.sp)
                }
            } else {
                // تو تب System Logs دکمه Force Sync رو نشون بده (برای تست)
                Button(
                    onClick = {
                        // ساختن یک درخواست یک‌باره برای Worker و اجرای فوری اون
                        val syncRequest = OneTimeWorkRequestBuilder<LogSyncWorker>().build()
                        WorkManager.getInstance(context).enqueue(syncRequest)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // رنگ سبز
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Force Sync", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- بخش Tab ها (انتخاب بین UDP و System) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF282A36))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // تب UDP
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (!showSystemLogs) Color(0xFF44475A) else Color.Transparent)
                    .clickable { showSystemLogs = false }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Crane Data",
                    color = if (!showSystemLogs) Color.White else Color.Gray,
                    fontSize = 13.sp,
                    fontWeight = if (!showSystemLogs) FontWeight.Bold else FontWeight.Normal
                )
            }

            // تب System Logs
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (showSystemLogs) Color(0xFF44475A) else Color.Transparent)
                    .clickable { showSystemLogs = true }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "System Logs",
                    color = if (showSystemLogs) Color.White else Color.Gray,
                    fontSize = 13.sp,
                    fontWeight = if (showSystemLogs) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color.DarkGray)
        Spacer(modifier = Modifier.height(8.dp))

        // --- بخش نمایش لیست لاگ‌ها ---
        Box(modifier = Modifier.fillMaxSize()) {

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true
            ) {
                // اگر تب سیستم انتخاب شده بود:
                if (showSystemLogs) {
                    items(systemLogs, key = { it.id }) { sysLog ->
                        // رنگ‌بندی بر اساس وضعیت لاگ سیستم
                        val textColor = when(sysLog.status) {
                            "ERROR" -> Color(0xFFFF5555) // قرمز
                            "SUCCESS" -> Color(0xFF50FA7B) // سبز
                            "INFO" -> Color(0xFF8BE9FD) // آبی روشن
                            else -> Color(0xFFF8F8F2) // سفید
                        }

                        // در اینجا فرض کردم SystemLog پراپرتی‌های timestamp و message رو داره
                        Text(
                            text = "[System] [${sysLog.timestamp}] [${sysLog.status}] : ${sysLog.message}",
                            color = textColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                // اگر تب UDP انتخاب شده بود (کد خودت):
                else {
                    items(udpLogs, key = { it.id }) { log ->

                        val textColor = when(log.logType) {
                            logType.error -> Color(0xFFFF5555)
                            logType.craneAlarm -> Color(0xFFFFB86C)
                            logType.received -> Color(0xFF50FA7B)
                            logType.sent -> Color(0xFF8BE9FD)
                            logType.info -> Color(0xFFF1FA8C)
                            else -> Color(0xFFF8F8F2)
                        }

                        Text(
                            text = "[${log.timestamp}] [${log.logType}] : ${log.message}",
                            color = textColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            // دکمه شناور رفتن به پایین (بدون تغییر)
            this@Column.AnimatedVisibility(
                visible = !isUserAtBottom,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    containerColor = Color(0xFF44475A),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(R.drawable.down),
                        contentDescription = "Scroll down"
                    )
                }
            }
        }
    }
}