package com.tivanco.hymaco.appUI.settingsScreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tivanco.hymaco.customViews.customToast.LocalToastManager
import com.tivanco.hymaco.dataClass.PreferencesKeys
import com.tivanco.hymaco.dataClass.ToastData
import com.tivanco.hymaco.utils.AppPreferences
import com.tivanco.hymaco.utils.PrefsManager
import com.tivanco.hymaco.utils.ExitDialog
import com.tivanco.hymaco.utils.ResetDialog
import com.tivanco.hymaco.utils.backClickAction
import com.tivanco.hymaco.utils.exit
import com.tivanco.hymaco.utils.getCurrentSettings
import com.tivanco.hymaco.utils.hasSavedSettings
import com.tivanco.hymaco.utils.resetSettings
import com.tivanco.hymaco.utils.saveChanges

@Composable
fun SettingsScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val toastManager = LocalToastManager.current
    val keys = PreferencesKeys()

    //--------------- DIALOG STATES ---------------//
    var showExitDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var needsToReset by remember { mutableStateOf(hasSavedSettings(context)) }

    //--------------- BASELINE STATES (مقادیر ذخیره شده در حافظه) ---------------//
    // این متغیرها مقدار فعلی حافظه رو نگه میدارن تا بتونیم با متنِ در حال تایپ مقایسه کنیم
    var savedSsid by remember { mutableStateOf(AppPreferences.getString(context, keys.wifiSsid, "")) }
    var savedPass by remember { mutableStateOf(AppPreferences.getString(context, keys.wifiPass, "")) }
    var savedInterval by remember { mutableStateOf(AppPreferences.getString(context, keys.interval, "")) }
    var savedHost by remember { mutableStateOf(AppPreferences.getString(context, keys.mqttHost, "")) }
    var savedPort by remember { mutableStateOf(AppPreferences.getString(context, keys.mqttPort, "")) }
    var savedName by remember { mutableStateOf(PrefsManager.getCustomName(context)) }

    //--------------- UI STATES (مقادیر داخل تکست‌فیلدها) ---------------//
    val ssid = remember { mutableStateOf(TextFieldValue(savedSsid)) }
    val pass = remember { mutableStateOf(TextFieldValue(savedPass)) }
    val interval = remember { mutableStateOf(TextFieldValue(savedInterval)) }
    val mqttHost = remember { mutableStateOf(TextFieldValue(savedHost)) }
    val mqttPort = remember { mutableStateOf(TextFieldValue(savedPort)) }
    val customNameState = remember { mutableStateOf(TextFieldValue(savedName)) }

    //--------------- LOGIC STATES ---------------//
    // مقایسه هوشمند: آیا متن تایپ شده با مقدار ذخیره شده فرق داره؟
    val hasUnsavedChanges by remember {
        derivedStateOf {
            ssid.value.text.trim() != savedSsid ||
                    pass.value.text.trim() != savedPass ||
                    interval.value.text.trim() != savedInterval ||
                    mqttHost.value.text.trim() != savedHost ||
                    mqttPort.value.text.trim() != savedPort ||
                    customNameState.value.text.trim() != savedName
        }
    }

    val currentSettings = getCurrentSettings(ssid, pass, interval, mqttHost, mqttPort, customNameState)

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {
        val bottomPadding = 10.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Scaffold(
            modifier = Modifier.padding(bottom = bottomPadding),
            containerColor = Color(0xFFF5F5F5),
            topBar = {
                SettingsTopBar(
                    onBackClick = {
                        backClickAction(navController, hasUnsavedChanges) {
                            showExitDialog = it
                        }
                    },
                    onSaveClick = {
                        if (hasUnsavedChanges) {
                            // ذخیره در حافظه
                            saveChanges(context, toastManager, currentSettings)

                            // آپدیت کردن مقادیر پایه (این کار باعث میشه hasUnsavedChanges خودکار false بشه)
                            savedSsid = ssid.value.text.trim()
                            savedPass = pass.value.text.trim()
                            savedInterval = interval.value.text.trim()
                            savedHost = mqttHost.value.text.trim()
                            savedPort = mqttPort.value.text.trim()
                            savedName = customNameState.value.text.trim()

                            needsToReset = hasSavedSettings(context)
                        } else {
                            toastManager.showToast(ToastData("تغییراتی برای ذخیره وجود ندارد"))
                        }
                    }
                )
            },
            bottomBar = {
                SettingsBottomBar(
                    onResetClick = {
                        if (needsToReset) showResetDialog = true
                    }
                )
            }
        ) { innerPadding ->
            Settings(
                modifier = Modifier.padding(innerPadding),
                focusManager = focusManager,
                ssid = ssid,
                pass = pass,
                interval = interval,
                mqttHost = mqttHost,
                mqttPort = mqttPort,
                customName = customNameState
            )
        }

        if (showExitDialog) {
            ExitDialog(
                onExit = {
                    showExitDialog = false
                    exit(navController)
                },
                onSaveAndExit = {
                    saveChanges(context, toastManager, currentSettings)
                    showExitDialog = false
                    exit(navController)
                },
                onDismiss = { showExitDialog = false }
            )
        }

        if (showResetDialog) {
            ResetDialog(
                onReset = {
                    // پاک کردن حافظه و تکست‌فیلدها
                    resetSettings(context, ssid, pass, interval, mqttHost, mqttPort)

                    // آپدیت کردن مقادیر پایه به استرینگ خالی (یا مقادیر دیفالت) تا دکمه سیو خاموش بشه
                    savedSsid = ""
                    savedPass = ""
                    savedInterval = ""
                    savedHost = ""
                    savedPort = ""

                    needsToReset = hasSavedSettings(context)
                    showResetDialog = false
                },
                onDismiss = { showResetDialog = false }
            )
        }
    }

    BackHandler { backClickAction(navController, hasUnsavedChanges) { showExitDialog = it } }
}