package com.tivanco.hymaco.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.NavHostController
import com.tivanco.hymaco.customViews.CustomDialog
import com.tivanco.hymaco.customViews.customToast.ToastManager
import com.tivanco.hymaco.dataClass.DialogButton
import com.tivanco.hymaco.dataClass.PreferencesKeys
import com.tivanco.hymaco.dataClass.Screen
import com.tivanco.hymaco.dataClass.SettingsData
import com.tivanco.hymaco.dataClass.ToastData

// OPENS A DIALOG TO RESET SAVED SETTINGS
@Composable
fun ResetDialog(
    onDismiss: () -> Unit,
    onReset: () -> Unit
) {
    CustomDialog(
        title = "بازنشانی تنظیمات",
        text = "آیا از بازنشانی تنظیمات مطمئنید؟این عمل قابل بازگشت نیست",
        buttons = listOf(
            DialogButton("انصراف") { onDismiss() },
            DialogButton("بازنشانی") { onReset() }
        ),
        onDismissRequest = { onDismiss() }
    )
}

// OPENS A DIALOG TO EXIT FROM SETTINGS SCREEN
@Composable
fun ExitDialog(
    onExit: () -> Unit = {},
    onSaveAndExit: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    CustomDialog(
        title = "خروج بدون ذخیره؟",
        text = "تغییرات ذخیره نشده از بین خواهند رفت.",
        buttons = listOf(
            DialogButton("انصراف") { onDismiss() },
            DialogButton("ذخیره و خروج") { onSaveAndExit() },
            DialogButton("خروج") { onExit() }
        ),
        onDismissRequest = { onDismiss() }
    )
}

// GET CURRENT SETTINGS TO SAVE THEM IN SHARED PREFERENCES
fun getCurrentSettings(
    ssid: MutableState<TextFieldValue>,
    pass: MutableState<TextFieldValue>,
    interval: MutableState<TextFieldValue>,
    mqttHost: MutableState<TextFieldValue>,
    mqttPort: MutableState<TextFieldValue>,
    customName: MutableState<TextFieldValue>
) = SettingsData(
    ssid.value.text,
    pass.value.text,
    interval.value.text,
    mqttHost.value.text,
    mqttPort.value.text,
    customName.value.text
)

// RESET ALL SAVED SETTINGS
fun resetSettings(
    context: Context,
    ssid: MutableState<TextFieldValue>,
    pass: MutableState<TextFieldValue>,
    interval: MutableState<TextFieldValue>,
    mqttHost: MutableState<TextFieldValue>,
    mqttPort: MutableState<TextFieldValue>,
) {
    AppPreferences.clear(context)
    ssid.value = TextFieldValue("")
    pass.value = TextFieldValue("")
    interval.value = TextFieldValue("")
    mqttHost.value = TextFieldValue("")
    mqttPort.value = TextFieldValue("")
}

// SAVE NEW SETTINGS IN SHARED PREFERENCES
val preferencesKeys = PreferencesKeys()
fun saveChanges(
    context: Context,
    toastManager: ToastManager,
    settings: SettingsData
) {
    PrefsManager.saveCustomName(context, settings.customName)

    AppPreferences.putString(
        context = context,
        key = preferencesKeys.wifiSsid,
        value = settings.ssid
    )
    AppPreferences.putString(
        context = context,
        key = preferencesKeys.wifiPass,
        value = settings.pass
    )
    AppPreferences.putString(
        context = context,
        key = preferencesKeys.interval,
        value = settings.interval
    )
    AppPreferences.putString(
        context = context,
        key = preferencesKeys.mqttHost,
        value = settings.mqttHost
    )
    AppPreferences.putString(
        context = context,
        key = preferencesKeys.mqttPort,
        value = settings.mqttPort
    )
    toastManager.showToast(ToastData("تغییرات با موفقیت ذخیره شدند"))
}

// EXIT FROM SETTINGS SCREEN
fun exit(navController: NavHostController) {
    navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } }
}

// CHECK WHETHER THE USER CHANGED THE SETTINGS OR NOT
fun hasSavedSettings(context: Context): Boolean {
    val keys = PreferencesKeys()
    return AppPreferences.getString(context, keys.wifiSsid, "").isNotEmpty() ||
            AppPreferences.getString(context, keys.wifiPass, "").isNotEmpty() ||
            AppPreferences.getString(context, keys.interval, "").isNotEmpty() ||
            AppPreferences.getString(context, keys.mqttHost, "").isNotEmpty() ||
            AppPreferences.getString(context, keys.mqttPort, "").isNotEmpty() ||
            AppPreferences.getString(context, keys.customCraneName, "").isNotEmpty()
}

fun backClickAction(navController: NavHostController, hasUnsavedChanges: Boolean, onBack: (Boolean) -> Unit) {
    if (hasUnsavedChanges) onBack(true)
    else exit(navController)
}