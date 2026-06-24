package com.tivanco.hymaco.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun WifiStateListener(
    onWifiStateChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var previousState by remember { mutableStateOf<Boolean?>(null) }

    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mainHandler = Handler(Looper.getMainLooper())

        // فقط دنبال شبکه‌هایی می‌گردیم که از نوع وای‌فای باشن
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // ۱. بایند کردن اپلیکیشن به وای‌فای (حیاتی برای ارتباط UDP با سخت‌افزار)
                connectivityManager.bindProcessToNetwork(network)

                // ۲. اطلاع به UI در ترد اصلی
                if (previousState != true) {
                    mainHandler.post {
                        onWifiStateChanged(true)
                    }
                    previousState = true
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                // ۱. آزاد کردن اپلیکیشن از وای‌فای قطع شده
                if (connectivityManager.boundNetworkForProcess == network) {
                    connectivityManager.bindProcessToNetwork(null)
                }

                // ۲. اطلاع به UI در ترد اصلی
                if (previousState != false) {
                    mainHandler.post {
                        onWifiStateChanged(false)
                    }
                    previousState = false
                }
            }
        }

        // ثبت لیسنر
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // وقتی از صفحه خارج میشیم، لیسنر رو حذف و بایند رو پاک می‌کنیم
        onDispose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            connectivityManager.bindProcessToNetwork(null)
        }
    }
}