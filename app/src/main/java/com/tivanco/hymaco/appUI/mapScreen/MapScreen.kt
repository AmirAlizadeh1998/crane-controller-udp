package com.tivanco.hymaco.appUI.mapScreen
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.net.ConnectivityManager
//import android.net.Network
//import android.net.NetworkCapabilities
//import android.net.NetworkRequest
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.core.content.ContextCompat
//import androidx.core.graphics.createBitmap
//import com.carto.styles.AnimationStyleBuilder
//import com.carto.styles.AnimationType
//import com.carto.styles.MarkerStyleBuilder
//import com.tivanco.hymaco.R
//import org.neshan.common.model.LatLng
//import org.neshan.mapsdk.MapView
//import org.neshan.mapsdk.internal.utils.BitmapUtils
//import org.neshan.mapsdk.model.Marker
//
//@Composable
//fun MapScreen(
//    modifier: Modifier = Modifier,
//    lat: Double,
//    lng: Double,
//    alt: Double,
//    speed: Double
//) {
//    val context = LocalContext.current
//
//    // وضعیت مجوزها
//    val permissions = arrayOf(
//        Manifest.permission.ACCESS_FINE_LOCATION,
//        Manifest.permission.ACCESS_COARSE_LOCATION
//    )
//    var isLocationGranted by remember {
//        mutableStateOf(
//            permissions.all {
//                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
//            }
//        )
//    }
//
//    // وضعیت دیتای موبایل
//    var isNetworkReady by remember { mutableStateOf(false) }
//
//    val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestMultiplePermissions()
//    ) { result ->
//        val granted = result.values.all { it }
//        isLocationGranted = granted
//        val message = if (granted) "مجوز تایید شد ✅" else "مجوز رد شد ❌"
//        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//    }
//
//    // درخواست مجوز
//    LaunchedEffect(Unit) {
//        if (!isLocationGranted) {
//            permissionLauncher.launch(permissions)
//        }
//    }
//
//    DisposableEffect(Unit) {
//        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//        // ۱. اینجا فقط اینترنت رو می‌خوایم، حرفی از VALIDATED نمی‌زنیم تا کرش نکنه
//        val networkRequest = NetworkRequest.Builder()
//            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//            .build()
//
//        val networkCallback = object : ConnectivityManager.NetworkCallback() {
//
//            // ۲. به جای onAvailable، از onCapabilitiesChanged استفاده می‌کنیم
//            // چون اندروید اول وصل میشه (onAvailable) بعد تست می‌کنه ببینه پینگ داره یا نه (اینجا صدا زده میشه)
//            override fun onCapabilitiesChanged(
//                network: Network,
//                networkCapabilities: NetworkCapabilities
//            ) {
//                super.onCapabilitiesChanged(network, networkCapabilities)
//
//                // ۳. چک می‌کنیم که آیا سیستم‌عامل اینترنت رو تایید کرده (پینگ داره) یا نه
//                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
//
//                    // پیدا کردن نوع شبکه‌ای که وصل شده برای لاگ زدن
//                    val isWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
//                    val netName = if (isWifi) "وای‌فای" else "دیتای موبایل"
//
//                    Log.d("MapScreen", "شبکه اینترنت‌دار و تایید شده ($netName) پیدا شد! هدایت ترافیک نقشه...")
//
//                    // کل برنامه‌ رو می‌بندیم به این شبکه‌ی تایید شده
//                    connectivityManager.bindProcessToNetwork(network)
//                    isNetworkReady = true
//                }
//            }
//
//            override fun onLost(network: Network) {
//                super.onLost(network)
//                Log.d("MapScreen", "شبکه اینترنت‌دار قطع شد!")
//                // اگه شبکه قطع شد، برمی‌گردونیم به حالت پیش‌فرض
//                connectivityManager.bindProcessToNetwork(null)
//                isNetworkReady = false
//            }
//        }
//
//        connectivityManager.requestNetwork(networkRequest, networkCallback)
//
//        // وقتی این صفحه بسته شد (کامپوز از بین رفت)، شبکه رو آزاد می‌کنیم
//        onDispose {
//            connectivityManager.unregisterNetworkCallback(networkCallback)
//            connectivityManager.bindProcessToNetwork(null)
//        }
//    }
//
//    if (isNetworkReady) {
//        NeshanMapView(
//            modifier = modifier.fillMaxSize(),
//            isLocationEnabled = isLocationGranted,
//            targetLat = lat,
//            targetLng = lng
//        )
//    } else {
//        // تغییر متن لودینگ
//        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            CircularProgressIndicator()
//            Text(text = "در حال بارگذاری نقشه...", modifier = Modifier.align(Alignment.BottomCenter))
//        }
//    }
//}
//
//@Composable
//fun NeshanMapView(
//    modifier: Modifier = Modifier,
//    isLocationEnabled: Boolean,
//    targetLat: Double,
//    targetLng: Double
//) {
//    val context = LocalContext.current
//    var carMarker by remember { mutableStateOf<Marker?>(null) }
//
//    Surface {
//        AndroidView(
//            modifier = modifier,
//            factory = { ctx ->
//                // ۲. بخش Factory فقط یک بار همون اول کار اجرا میشه
//                MapView(ctx).apply {
//                    setZoom(15f, 0f)
//
//                    if (isLocationEnabled) {
//                        settings.isMyLocationButtonEnabled = true
//                    }
//
//                    // ساختن مارکر اولیه همون دفعه اول
//                    val initialLocation = LatLng(targetLat, targetLng)
//                    val newMarker = createMarker(context, initialLocation)
//
//                    // اضافه کردن مارکر به نقشه
//                    addMarker(newMarker)
//
//                    // 💥 اینجاست که مارکر رو ذخیره می‌کنیم برای استفاده‌های بعدی!
//                    carMarker = newMarker
//                }
//            },
//            update = { mapView ->
//                // ۳. بخش Update هر بار که $lat$ یا $lng$ عوض بشن اجرا میشه
//                val targetLocation = LatLng(targetLat, targetLng)
//
//                // 🚀 حرکت خفن: به جای پاک کردن، فقط مختصات مارکر ذخیره شده رو آپدیت می‌کنیم
//                carMarker?.latLng = targetLocation
//
//                // حرکت نرم دوربین به سمت موقعیت جدید ماشین
//                mapView.moveCamera(targetLocation, 0.5f)
//            }
//        )
//    }
//}
//
//// تابع ساخت مارکر (با انیمیشن و آیکون ایمن)
//private fun createMarker(context: Context, loc: LatLng): Marker {
//    // تنظیم انیمیشن
//    val animStBl = AnimationStyleBuilder()
//    animStBl.fadeAnimationType = AnimationType.ANIMATION_TYPE_SMOOTHSTEP
//    animStBl.sizeAnimationType = AnimationType.ANIMATION_TYPE_SPRING
//    animStBl.phaseInDuration = 0.5f
//    animStBl.phaseOutDuration = 0.5f
//    val animSt = animStBl.buildStyle()
//
//    // ساخت استایل مارکر
//    val markStCr = MarkerStyleBuilder()
//    markStCr.size = 30f
//
//    // ✅ استفاده از متد کمکی برای تبدیل وکتور به بیت‌مپ (جلوگیری از کرش)
//    val bitmap = getBitmapFromVectorDrawable(context, R.drawable.location)
//    if (bitmap != null) {
//        markStCr.bitmap = BitmapUtils.createBitmapFromAndroidBitmap(bitmap)
//    }
//
//    markStCr.animationStyle = animSt
//    val markSt = markStCr.buildStyle()
//
//    return Marker(loc, markSt)
//}
//
//// ✅ متد کمکی حیاتی برای تبدیل Vector/XML به Bitmap
//fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
//    val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
//    val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
//    val canvas = Canvas(bitmap)
//    drawable.setBounds(0, 0, canvas.width, canvas.height)
//    drawable.draw(canvas)
//    return bitmap
//}
