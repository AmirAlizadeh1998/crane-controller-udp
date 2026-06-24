package com.tivanco.hymaco.appUI.mainScreen

import android.app.Activity
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.tivanco.hymaco.R
import com.tivanco.hymaco.customViews.customToast.LocalToastManager
import com.tivanco.hymaco.dataClass.Screen
import com.tivanco.hymaco.dataClass.ToastData
import com.tivanco.hymaco.utils.WifiStateListener
import com.tivanco.hymaco.viewModel.ConnectionViewModel
import com.tivanco.hymaco.viewModel.CraneControlViewModel
import com.tivanco.hymaco.viewModel.MainUiViewModel

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun HomeScreen(
    navController: NavController,
    uiViewModel: MainUiViewModel,
    connectionViewModel: ConnectionViewModel,
    craneControlViewModel: CraneControlViewModel
) {
    val toastManager = LocalToastManager.current
    val context = LocalContext.current
    val activity = context as? Activity
    val btnPressedVm = uiViewModel.btnReady.collectAsState().value
    var isWifiConnected by remember { mutableStateOf(false) }

    // چرخه حیات رو اینجا می‌گیریم
    val lifecycleOwner = LocalLifecycleOwner.current

    WifiStateListener { connected ->
        // استیت رو همیشه آپدیت می‌کنیم که اپلیکیشن بدونه وضعیت چیه
        isWifiConnected = connected

        // اینجا چک می‌کنیم اگه اپلیکیشن تو بک‌گراند نیست (حداقل RESUMED هست)، توست نشون بده
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            if (!connected) {
                toastManager.showToast(
                    ToastData(
                        message = "اتصال WIFI قطع شد",
                        backgroundColor = Color(0xFFFC4646)
                    )
                )
            } else {
                toastManager.showToast(
                    ToastData(
                        message = "به WIFI متصل شدید",
                        backgroundColor = Color(0xFF66BB6A)
                    )
                )
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val bottomPadding =
            10.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color(0xFFCFD8DC),
                topBar = {
                    HomeTopBar(
                        connectionViewModel = connectionViewModel,
                        craneControlViewModel = craneControlViewModel,
                        isWifiConnected = isWifiConnected,
                        navController = navController,
                        uiViewModel = uiViewModel
                    )
                },
                bottomBar = {
                    HomeBottomBar(
                        uiViewModel = uiViewModel,
                        onClutchPress = {
                            uiViewModel.setClutchState(true)
                            Log.i(
                                "HomeScreen",
                                "isClutchPressed: ${uiViewModel.isClutchPressed.value}"
                            )
                        },
                        onClutchRelease = {
                            uiViewModel.setClutchState(false)
                            Log.i(
                                "HomeScreen",
                                "isClutchPressed: ${uiViewModel.isClutchPressed.value}"
                            )
                        }
                    )
                },
                floatingActionButton = {
                    HomeFab(
                        R.drawable.sparkling,
                        !btnPressedVm,
                        Color(0xffF2951B),
                        Color(0xff122442)
                    ) { navController.navigate(Screen.Support.route) }
                },
                floatingActionButtonPosition = FabPosition.Start
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // ---------- Main Content ----------
                    Home(
                        craneControlViewModel = craneControlViewModel,
                        connectionViewModel = connectionViewModel,
                        uiViewModel = uiViewModel
                    )
                }
            } // END OF SCAFFOLD
        } // END OF ROOT BOX
    } // END OF CompositionLocalProvider

// ---------- BackHandler ----------
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    BackHandler {
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastBackPressTime < 2000) activity?.finish()
        else {
            lastBackPressTime = currentTime
            toastManager.showToast(ToastData("برای خروج دوباره دکمه برگشت را بزنید"))
        }
    }

} // END OF HomeScreen
