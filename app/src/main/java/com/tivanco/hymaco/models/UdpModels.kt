package com.tivanco.hymaco.models

data class CraneCommand(
    var clutch: Int = 0,
    var turn: Int = 0,
    var lift: Int = 0,
    var telescope: Int = 0,
    var fixFront: Int = 0,
    var fixBack: Int = 0
) {
    // یه تابع کمکی مینویسیم که این آبجکت رو تبدیل کنه به اون استرینگ یا بایتی که جرثقیل می‌فهمه
    fun toFormattedString(): String {
        val crc = clutch + turn + lift + telescope + fixFront + fixBack
        return "key:${clutch}${turn}${lift}${telescope}${fixFront}${fixBack}${crc}"
    }
}