package com.tivanco.hymaco.appUI.mainScreen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tivanco.hymaco.R
import com.tivanco.hymaco.customViews.RockerToggle
import com.tivanco.hymaco.customViews.customToast.LocalToastManager
import com.tivanco.hymaco.dataClass.ConnectionEvent
import com.tivanco.hymaco.dataClass.Screen
import com.tivanco.hymaco.dataClass.Status
import com.tivanco.hymaco.dataClass.ToastData
import com.tivanco.hymaco.utils.BlinkingErrorText
import com.tivanco.hymaco.utils.playSound
import com.tivanco.hymaco.utils.responsiveSizes
import com.tivanco.hymaco.viewModel.ConnectionViewModel
import com.tivanco.hymaco.viewModel.CraneControlViewModel
import com.tivanco.hymaco.viewModel.MainUiViewModel

private const val TAG = "HomeTopBar"

@Composable
fun HomeTopBar(
    navController: NavController,
    uiViewModel: MainUiViewModel,
    connectionViewModel: ConnectionViewModel,
    craneControlViewModel: CraneControlViewModel,
    isWifiConnected: Boolean,
) {
    val context = LocalContext.current
    val toastManager = LocalToastManager.current
    val btnReadyVm by uiViewModel.btnReady.collectAsStateWithLifecycle()

    val connectionStatus by connectionViewModel.connectionStatus.collectAsStateWithLifecycle()
    val statusMessage by connectionViewModel.statusMessage.collectAsStateWithLifecycle()

    val filterError by craneControlViewModel.dirtyFilter.collectAsStateWithLifecycle()
    val highVoltageError by craneControlViewModel.highVoltage.collectAsStateWithLifecycle()
    val overWeightError by craneControlViewModel.overWeight.collectAsStateWithLifecycle()
    val beltError by craneControlViewModel.seatBelt.collectAsStateWithLifecycle()
    val pressureError by craneControlViewModel.overPressure.collectAsStateWithLifecycle()

    // اول لیست خطاهایی که واقعاً رخ دادن رو می‌سازیم
    val activeErrors =
        remember(filterError, beltError, highVoltageError, pressureError, overWeightError) {
            buildList {
                if (filterError == "1") add("فیلتر کثیف")
                if (beltError == "1") add("کمربند باز")
                if (highVoltageError == "1") add("ولتاژ بالا")
                if (pressureError == "1") add("فشار هیدرولیک بالا")
                if (overWeightError == "1") add("وزن سبد بالا")
            }
        }

    LaunchedEffect(statusMessage) {
        Log.d(TAG, "status: $statusMessage")
    }

    LaunchedEffect(Unit) {
        uiViewModel.connectionEvent.collect { event ->
            when (event) {
                ConnectionEvent.CONNECTED -> playSound(context, R.raw.connected)
                ConnectionEvent.DISCONNECTED -> playSound(context, R.raw.disconnectd)
            }
        }
    }

    // ---------- Header Box ----------
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFEDF2F7),
                            Color(0xFFD7DFE9)
                        )
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color.Black.copy(alpha = 0.2f)
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFF7F9FB), Color(0xFFE3EAF3))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Logos()
            }

            // ---------- TOOLBAR ----------
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFE8E8E8),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                //-------------- ToggleButton(TOGGLE DIRECTION START -> END) --------------
                RockerToggle(
                    isOn = connectionStatus == Status.CONNECTED,
                    onToggle = { isToggled ->
                        if (!isWifiConnected) {
                            toastManager.showToast(ToastData("به WIFI متصل نیستید"))
                            return@RockerToggle
                        }
                        if (isToggled) connectionViewModel.startConnection() else {
                            connectionViewModel.stopConnection()
                            craneControlViewModel.resetErrors()
                        }
                    },
                    status = connectionStatus,
                    enabled = connectionStatus != Status.CONNECTING || !btnReadyVm,
                    width = responsiveSizes().rockerSize,
                    height = responsiveSizes().rockerSize / 2,
                    onIcon = R.drawable.router_connect,
                    offIcon = R.drawable.router_disconnect,
                    onColor = colorResource(R.color.green),
                    offColor = colorResource(R.color.red)
                )

                // -------- Monitoring Icon --------
                IconButton(
                    onClick = {
                        navController.navigate(Screen.Logs.route)
                    },
                    enabled = !btnReadyVm
                ) {
                    Icon(
                        painter = painterResource(R.drawable.scorecard),
                        contentDescription = "Monitoring"
                    )
                }

                // -------- Log Icon --------
                IconButton(
                    onClick = {
                        navController.navigate(Screen.AppLogs.route)
                    },
                    enabled = !btnReadyVm
                ) {
                    Icon(
                        painter = painterResource(R.drawable.log),
                        contentDescription = "Logs"
                    )
                }

                // -------- Settings Icon --------
                IconButton(
                    onClick = {
                        navController.navigate(Screen.Settings.route)
                    },
                    enabled = !btnReadyVm
                ) {
                    Icon(
                        painter = painterResource(R.drawable.settings),
                        contentDescription = "Settings"
                    )
                }
            } // END OF ROW

            // SHOW RECEIVED MESSAGE FOR DEBUG AND TEST
//        Text(
//            text = debugMsg,
//            fontSize = 10.sp
//        )
        }
        // فقط اگه خطایی وجود داشت، اون باکس تیره رو نشون بده
        if (activeErrors.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF555555)),
                elevation = CardDefaults.cardElevation(10.dp),
                border = BorderStroke(1.dp, color = Color.Gray)
            ) {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color(0xFFF1F1F1))
                        .padding(12.dp),
                    // این تنظیمات باعث میشه فاصله‌ها خودکار و تمیز پخش بشن
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    activeErrors.forEach { errorTitle ->
                        // دیگه نیازی به حلقه‌های تو در تو و chunked نیست!
                        BlinkingErrorText(text = errorTitle, isError = true)
                    }
                }
            }
        }
    }
}