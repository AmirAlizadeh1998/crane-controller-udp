package com.tivanco.hymaco.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun responsiveSizes(): ResponsiveSizes {
    val config = LocalConfiguration.current
    val density = LocalDensity.current.density
    val fontScale = config.fontScale
    val screenWidthDp = config.screenWidthDp

    // ضریب کلی مقیاس برای عناصر گرافیکی (بر اساس عرض صفحه)
    val widthFactor = when {
        screenWidthDp < 360 -> 0.8f
        screenWidthDp in 360..400 -> 1.0f
        screenWidthDp in 401..500 -> 1.2f
        screenWidthDp in 501..600 -> 1.4f
        else -> 1.6f
    }

    // ضریب برای تراکم (density)
    val densityFactor = when {
        density < 1.5f -> 0.9f
        density in 1.5f..2.5f -> 1.0f
        density in 2.5f..3.5f -> 1.1f
        else -> 1.25f
    }

    // ضریب کلی نهایی (ترکیب عرض و چگالی)
    val scale = widthFactor * densityFactor
    Log.i(
        "ResponsiveSizes",
        "density=$density, widthDp=$screenWidthDp, scale=$scale"
    )

    return ResponsiveSizes(
        buttonSize = (70 * scale).dp,
        arrowSize = (80 * scale).dp,
        ledSize = (15 * scale).dp,
        rockerSize = (48 * scale).dp,
        iconSize = (24 * scale).dp,
        fontSize = (14 * fontScale * scale).sp,
        labelFontSize = (12 * fontScale * scale).sp,
        toastFontSize = (14 * fontScale * scale).sp,
        signalFontSize = (11 * fontScale * scale).sp,
        imageSize = (70 * scale).dp,
        bannerHeight = (100 * scale).dp
    )
}

data class ResponsiveSizes(
    val buttonSize: Dp,
    val arrowSize: Dp,
    val ledSize: Dp,
    val rockerSize: Dp,
    val iconSize: Dp,
    val fontSize: TextUnit,
    val labelFontSize: TextUnit,
    val toastFontSize: TextUnit,
    val signalFontSize: TextUnit,
    val imageSize: Dp,
    val bannerHeight: Dp
)