package com.tivanco.hymaco.appUI.mainScreen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tivanco.hymaco.customViews.LedState
import com.tivanco.hymaco.customViews.LeftRightButton
import com.tivanco.hymaco.customViews.OpenCloseButton
import com.tivanco.hymaco.customViews.UpDownButton
import com.tivanco.hymaco.customViews.customToast.LocalToastManager
import com.tivanco.hymaco.dataClass.Status
import com.tivanco.hymaco.dataClass.ToastData
import com.tivanco.hymaco.utils.CraneNotificationHelper
import com.tivanco.hymaco.viewModel.ConnectionViewModel
import com.tivanco.hymaco.viewModel.CraneControlViewModel
import com.tivanco.hymaco.viewModel.MainUiViewModel

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun Home(
    modifier: Modifier = Modifier,
    uiViewModel: MainUiViewModel,
    craneControlViewModel: CraneControlViewModel,
    connectionViewModel: ConnectionViewModel
) {
    val context = LocalContext.current
    val toastManager = LocalToastManager.current
    val fixFValue by craneControlViewModel.fixF.collectAsStateWithLifecycle()
    val fixBValue by craneControlViewModel.fixB.collectAsStateWithLifecycle()

    // ✅ اصلاح ۲: خواندن وضعیت اتصال مستقیم از ویومدل (مطمئن‌تر از پارامتر ورودی)
    val localConnectionStatus by connectionViewModel.connectionStatus.collectAsStateWithLifecycle()
    val isConnected = localConnectionStatus == Status.CONNECTED

    // ---------- States ----------
    val scrollState = rememberSaveable(saver = ScrollState.Saver) {
        ScrollState(initial = 0)
    }

    var turnRight by remember { mutableStateOf(false) }
    var turnLeft by remember { mutableStateOf(false) }
    var teleUp by remember { mutableStateOf(false) }
    var teleDown by remember { mutableStateOf(false) }
    var liftUp by remember { mutableStateOf(false) }
    var liftDown by remember { mutableStateOf(false) }
    var fixFrontOpen by remember { mutableStateOf(false) }
    var fixFrontClose by remember { mutableStateOf(false) }
    var fixBackOpen by remember { mutableStateOf(false) }
    var fixBackClose by remember { mutableStateOf(false) }

    val btnReadyVm = uiViewModel.btnReady.collectAsState().value
    val isClutchPressed = uiViewModel.isClutchPressed.collectAsState().value

    // LED States
    val frontLedState = if (isConnected && fixFValue == "1") LedState.OPEN
    else if (isConnected && fixFValue == "0") LedState.CLOSED
    else LedState.OFF

    val backLedState = if (isConnected && fixBValue == "1") LedState.OPEN
    else if (isConnected && fixBValue == "0") LedState.CLOSED
    else LedState.OFF

    // محاسبه اینکه آیا دکمه‌ای زده شده یا نه
    val btnReady by remember(
        turnRight, turnLeft, liftUp, liftDown, teleUp, teleDown,
        fixFrontOpen, fixFrontClose, fixBackOpen, fixBackClose
    ) {
        derivedStateOf {
            turnRight || turnLeft || teleUp || teleDown ||
                    liftUp || liftDown || fixFrontOpen || fixFrontClose ||
                    fixBackOpen || fixBackClose
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            toastManager.showToast(ToastData("مجوز ارسال نوتیفیکیشن از سمت شما رد شد می توانید در تنظیمات دستگاه خود،آن را تغییر دهید"))
        }
    }

    // ---------- Effects ----------
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU = API 33
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        craneControlViewModel.notificationEvent.collect { message ->
            CraneNotificationHelper.showNotification(
                context = context,
                title = "هشدار سیستم",
                message = message
            )
        }
    }

    // 4. سینک کردن وضعیت دکمه‌ها با UI ViewModel
    LaunchedEffect(btnReady) {
        uiViewModel.setBtnReady(btnReady)
        if (!btnReady) uiViewModel.setClutchPressing(false)
    }

    // 5. ارسال دیتا به سرور (اصلاح شده و حیاتی) 🚀
    LaunchedEffect(
        btnReadyVm, isClutchPressed, isConnected, // اضافه کردن وابستگی‌ها
        turnRight, turnLeft, liftUp, liftDown,
        teleUp, teleDown, fixFrontOpen, fixFrontClose,
        fixBackOpen, fixBackClose
    ) {
        // مدیریت کلاچ اتوماتیک
        if (isClutchPressed && !btnReadyVm) uiViewModel.setClutchState(false)

        val turn = if (turnRight) 1 else if (turnLeft) 2 else 0
        val lift = if (liftUp) 1 else if (liftDown) 2 else 0
        val telescope = if (teleUp) 1 else if (teleDown) 2 else 0
        val fixFront = if (fixFrontOpen) 1 else if (fixFrontClose) 2 else 0
        val fixBack = if (fixBackOpen) 1 else if (fixBackClose) 2 else 0

        // دیتا رو به ویومدل بده. ویومدل خودش تصمیم میگیره کی بفرسته
        craneControlViewModel.updateCraneCommand(
            turn = turn,
            lift = lift,
            telescope = telescope,
            fixFront = fixFront,
            fixBack = fixBack,
            clutchState = isClutchPressed
        )
    }

    // ---------- UI Layout ----------
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
    ) {
        CompositionLocalProvider(LocalToastManager provides toastManager) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState, !btnReady),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ... (بقیه UI دقیقاً مثل کد خودت، بدون تغییر) ...
                // پنل ثابت کننده‌ها
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8E9BC), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OpenCloseButton(
                        text = "ثابت کننده جلو",
                        onOpen = { fixFrontOpen = true },
                        onClose = { fixFrontClose = true },
                        onRelease = { fixFrontOpen = false; fixFrontClose = false },
                        ledState = frontLedState
                    )
                    OpenCloseButton(
                        text = "ثابت کننده عقب",
                        onOpen = { fixBackOpen = true },
                        onClose = { fixBackClose = true },
                        onRelease = { fixBackOpen = false; fixBackClose = false },
                        ledState = backLedState
                    )
                }

                Spacer(Modifier.height(15.dp))

                // دکمه‌های بالابر و تلسکوپ
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UpDownButton(
                        text = "بالابر",
                        onUpClick = { liftUp = true },
                        onDownClick = { liftDown = true },
                        onRelease = { liftUp = false; liftDown = false }
                    )
                    UpDownButton(
                        text = "تلسکوپ",
                        onUpClick = { teleUp = true },
                        onDownClick = { teleDown = true },
                        onRelease = { teleUp = false; teleDown = false }
                    )
                }

                // دکمه‌های گردان
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    LeftRightButton(
                        text = "گردان",
                        onLeftClick = { turnLeft = true },
                        onRightClick = { turnRight = true },
                        onRelease = { turnLeft = false; turnRight = false }
                    )
                }

                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}