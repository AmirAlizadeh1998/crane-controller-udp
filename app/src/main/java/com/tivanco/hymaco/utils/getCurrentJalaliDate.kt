package com.tivanco.hymaco.utils

import java.util.Calendar
import java.util.Date
import java.util.Locale

fun getCurrentJalaliDate(): String {
    val date = Date()
    val cal = Calendar.getInstance()
    cal.time = date
    val gYear = cal.get(Calendar.YEAR)
    val gMonth = cal.get(Calendar.MONTH) + 1
    val gDay = cal.get(Calendar.DAY_OF_MONTH)

    val gDays = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    val jDays = intArrayOf(0, 31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

    val gy = gYear - 1600
    val gm = gMonth - 1
    val gd = gDay - 1

    var gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
    for (i in 0 until gm) gDayNo += gDays[i + 1]
    if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) gDayNo++
    gDayNo += gd

    var jDayNo = gDayNo - 79
    val jNp = jDayNo / 12053
    jDayNo %= 12053
    var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
    jDayNo %= 1461

    if (jDayNo >= 366) {
        jy += (jDayNo - 1) / 365
        jDayNo = (jDayNo - 1) % 365
    }

    var jm = 0
    var jd = 0
    for (i in 0..10) {
        val days = jDays[i + 1]
        if (jDayNo < days) {
            jm = i + 1
            jd = jDayNo + 1
            break
        }
        jDayNo -= days
    }
    if (jm == 0) { jm = 12; jd = jDayNo + 1 }

    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)
    val second = cal.get(Calendar.SECOND)

    return String.format(Locale.getDefault(), "%04d/%02d/%02d %02d:%02d:%02d", jy, jm, jd, hour, minute, second)
}