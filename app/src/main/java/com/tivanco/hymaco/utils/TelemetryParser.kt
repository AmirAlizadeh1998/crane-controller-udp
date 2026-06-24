package com.tivanco.hymaco.utils

import android.util.Log
import com.tivanco.hymaco.models.CraneAlarm
import com.tivanco.hymaco.models.CraneStatus
import com.tivanco.hymaco.models.CraneTelemetry
import java.util.Locale

class TelemetryParser {

    // نقشه خطاها رو اینجا به عنوان یه ثابت تعریف می‌کنیم
    companion object {
        private val ERROR_CODES = mapOf(
            1 to "فیلتر کثیف", 2 to "فشار هیدرولیک بالا", 3 to "وزن سبد بیش از حد",
            4 to "کمربند باز", 5 to "ولتاژ بالا", 6 to "خطای کلاچ"
        )
    }

    fun parse(rawMessage: String): CraneTelemetry {
        val parts = rawMessage.split("status:", "alarm:").filter { it.isNotBlank() }

        val statusList = parts.getOrNull(0)?.split(",")?.map { it.trim() } ?: emptyList()
        val alarmListIds = parts.getOrNull(1)?.split(",")?.map { it.trim() } ?: emptyList()

        val parsedStatus = parseStatus(statusList)
        val parsedAlarms = parseAlarms(alarmListIds)

        Log.d("TelemetryParser", "raw message: $rawMessage\n$parsedStatus\n$parsedAlarms")

        return CraneTelemetry(status = parsedStatus, alarms = parsedAlarms)
    }

    private fun parseStatus(statusList: List<String>): CraneStatus {
        return CraneStatus(
            workClock = normalizeWorkClock(statusList.getOrNull(0)?.toFloatOrNull() ?: 0f), // 76455
            oilIn = statusList.getOrNull(1) ?: "0", // 78.0
            oilOut = statusList.getOrNull(2) ?: "0", // 8.6
            tower = statusList.getOrNull(3) ?: "0", // 0
            fixF = statusList.getOrNull(4) ?: "0", // 1
            fixB = statusList.getOrNull(5) ?: "0", //  1
            rot1 = statusList.getOrNull(6) ?: "0", // 0
            rot2 = statusList.getOrNull(7) ?: "0", // 0
            gbState = statusList.getOrNull(8) ?: "0", // 0
            dirtyFilter = statusList.getOrNull(9) ?: "0",
            overPressure = statusList.getOrNull(10) ?: "0",
            overWeight = statusList.getOrNull(11) ?: "0",
            seatBelt = statusList.getOrNull(12) ?: "0",
            highVoltage = statusList.getOrNull(13) ?: "0",
            badClutch = statusList.getOrNull(14) ?: "0",
            greaseChange = statusList.getOrNull(15) ?: "0",
            hydroOilChange = statusList.getOrNull(16) ?: "0",
            gbOilChangeClock = normalizeWorkClock(statusList.getOrNull(17)?.toFloatOrNull() ?: 0f), // 25
            hydroOilChangeClock = normalizeWorkClock(statusList.getOrNull(18)?.toFloatOrNull() ?: 0f), // 25
        )
    }

    private fun parseAlarms(alarmListIds: List<String>): List<CraneAlarm> {
        val alarms = mutableListOf<CraneAlarm>() // alarm:0-0,0-0,3640-5,0-0,3649-5,0-0,0-0,0-0,0-0,4250-4,0-0,4255-1,0-0,0-0,0-0,0-0,0-0,0-0,0-0,4367-1,"
        alarmListIds.forEach { errorStr ->
            val errorParts = errorStr.split("-")
            if (errorParts.size >= 2) {
                val rawTime = errorParts[0]
                val code = errorParts[1].toIntOrNull()

                ERROR_CODES[code]?.let { errorText ->
                    alarms.add(
                        CraneAlarm(
                            message = errorText,
                            time = normalizeWorkClock(rawTime.toFloatOrNull() ?: 0f),
                            persianDate = getCurrentJalaliDate()
                        )
                    )
                }
            }
        }
        return alarms
    }

    private fun normalizeWorkClock(totalMinutes: Float): String {
        val hours = (totalMinutes / 60).toInt()
        val minutes = (totalMinutes % 60).toInt()
        return String.format(Locale.US, "%02d:%02d", hours, minutes)
    }
}
