package com.tivanco.hymaco.models

// این کلاس کل دیتای دریافتی از جرثقیل رو در خودش نگه میداره
data class CraneTelemetry(
    val status: CraneStatus,
    val alarms: List<CraneAlarm>
)

// این کلاس وضعیت لحظه‌ای جرثقیل رو نگه میداره
data class CraneStatus(
    val workClock: String = "00:00",
    val oilIn: String = "0",
    val oilOut: String = "0",
    val tower: String = "0",
    val fixF: String = "0",
    val fixB: String = "0",
    val rot1: String = "0",
    val rot2: String = "0",
    val gbState: String = "0",
    val dirtyFilter: String = "0",
    val overPressure: String = "0",
    val overWeight: String = "0",
    val seatBelt: String = "0",
    val highVoltage: String = "0",
    val badClutch: String = "0",
    val greaseChange: String = "0",
    val hydroOilChange: String = "0",
    val gbOilChangeClock: String = "0",
    val hydroOilChangeClock: String = "0",
    val errorsDeleted: Boolean = false,
    val noDeleteRespond: Boolean = false,
    val persianTime: String = "00:00",
    val error: String = "0",
)

// این کلاس یک آلارم خاص رو با جزئیاتش نگه میداره
data class CraneAlarm(
    val message: String,
    val time: String,
    val persianDate: String
)
