package com.tivanco.hymaco.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tivanco.hymaco.dataClass.SendCommand
import com.tivanco.hymaco.dataClass.Status
import com.tivanco.hymaco.models.CraneAlarm
import com.tivanco.hymaco.models.CraneCommand
import com.tivanco.hymaco.models.CraneStatus
import com.tivanco.hymaco.repository.UdpRepository
import com.tivanco.hymaco.utils.TelemetryParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val TAG = "CraneControl"

class CraneControlViewModel(
    private val repository: UdpRepository,
    private val parser: TelemetryParser
) : ViewModel() {

    private val intervalMs = 1000L
    private val _craneStatus = MutableStateFlow(CraneStatus())
    val craneStatus = _craneStatus.asStateFlow()

    private val _alarms = MutableStateFlow<List<CraneAlarm>>(emptyList())
    val alarms = _alarms.asStateFlow()

    private val _commandState = MutableStateFlow(CraneCommand())

    private val _notificationEvent = MutableSharedFlow<String>()
    val notificationEvent = _notificationEvent.asSharedFlow()

    val workClock = craneStatus.map { it.workClock }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "00:00")

    val oilIn = craneStatus.map { it.oilIn }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val oilOut = craneStatus.map { it.oilOut }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val tower = craneStatus.map { it.tower }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val fixF = craneStatus.map { it.fixF }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val fixB = craneStatus.map { it.fixB }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val rot1 = craneStatus.map { it.rot1 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val rot2 = craneStatus.map { it.rot2 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val gbState = craneStatus.map { it.gbState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val dirtyFilter = craneStatus.map { it.dirtyFilter }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val highVoltage = craneStatus.map { it.highVoltage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val overWeight = craneStatus.map { it.overWeight }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val seatBelt = craneStatus.map { it.seatBelt }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val overPressure = craneStatus.map { it.overPressure }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val gbOilChange = craneStatus.map { it.greaseChange }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val hydroOilChange = craneStatus.map { it.hydroOilChange }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val gbOilChangeClock = craneStatus.map { it.gbOilChangeClock }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val hydroOilChangeClock = craneStatus.map { it.hydroOilChangeClock }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

//    val errorsDeleted = craneStatus.map { it.errorsDeleted }
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
//
//    val noDeleteRespond = craneStatus.map { it.noDeleteRespond }
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val persianTime = craneStatus.map { it.persianTime }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "00:00")

    val error = craneStatus.map { it.error }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _isLogMode = MutableStateFlow(false)

    fun setLogMode(isActive: Boolean) {
        _isLogMode.value = isActive
    }

    init {
        viewModelScope.launch {
            repository.receivedMessages.collect { message ->
                Log.d(TAG, "modem sent: $message")
                if (message.startsWith("delete")) {
                    _alarms.value = emptyList()
                }

                try {
                    // ۱. پیام خام رو بده به مترجم
                    val telemetryData = parser.parse(message)
                    val connectionTime = repository.lastConnectionTime.value
                    val finalStatus = telemetryData.status.copy(persianTime = connectionTime)
                    val oldStatus = _craneStatus.value

                    // ۲. آبجکت تمیز رو بذار تو استیت
                    _craneStatus.value = finalStatus

                    // ۳. بررسی خطاهای ایندکس 9 تا 16 و ارسال نوتیفیکیشن
                    checkCriticalErrors(oldStatus, finalStatus)

                    // ۴. آپدیت هوشمندانه لیست آلارم‌ها
                    if (message.contains("alarm:")) {
                        Log.d(TAG, "alarm: $message")
                        _alarms.update { currentAlarms ->
                            telemetryData.alarms.map { newAlarm ->
                                val existingAlarm = currentAlarms.find {
                                    it.message == newAlarm.message && it.time == newAlarm.time
                                }
                                if (existingAlarm != null) {
                                    newAlarm.copy(persianDate = existingAlarm.persianDate)
                                } else {
                                    newAlarm
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "alarm not found")
                    }

                } catch (e: Exception) {
                    // 🔴 این لاگ بهت میگه دقیقاً کدوم خطِ پارسر داره گند می‌زنه!
                    Log.e(TAG, "Error parsing message: $message", e)
                }
            }
        }

        viewModelScope.launch {
            // ترکیب وضعیت دستورات با وضعیت اتصال (ارتباط)
            combine(_commandState, _isLogMode, repository.connectionStatus) { command, isLogMode, status ->
                // اینجا چک می‌کنیم اگه وضعیت اتصال CONNECTED بود، true برگردونه
                val isConnected = (status == Status.CONNECTED)
                Triple(command, isLogMode, isConnected)
            }.collectLatest { (command, isLogMode, isConnected) ->
                // به محض اینکه دستوری عوض شه یا اتصال قطع/وصل شه، کدهای قبلیِ داخل این بلاک Cancel میشن!

                if (isConnected) {
                    if (isLogMode) {
                        // --- مود لاگ ---
                        // اول یه استاتوس فوری بفرست که کاربر معطل نشه
                        repository.queueCommand(SendCommand.TextMessage("status"))

                        while (isActive) {
                            delay(1000L) // هر $1$ ثانیه
                            repository.queueCommand(SendCommand.TextMessage("status"))
                        }
                    } else {
                        // --- مود کنترل ---
                        val commandString = command.toFormattedString()
                        val sendCommand = SendCommand.KeyState(commandString)

                        // ارسال فوری دستور (به محض لمس دکمه یا تغییر استیت)
                        repository.queueCommand(sendCommand)
                        Log.i(TAG, "sent (Immediate): $commandString")

                        while (isActive) {
                            delay(intervalMs)
                            repository.queueCommand(sendCommand)
                            Log.i(TAG, "sent (Heartbeat): $commandString")
                        }
                    }
                } else {
                    Log.i(TAG, "UDP is disconnected or connecting. Sending paused.")
                }
            }
        }
    }

    fun updateCraneCommand(
        turn: Int,
        lift: Int,
        telescope: Int,
        fixFront: Int,
        fixBack: Int,
        clutchState: Boolean
    ) {
        val clutchVal = if (clutchState) 1 else 0

        _commandState.update { current ->
            current.copy(
                clutch = clutchVal,
                turn = turn,
                lift = lift,
                telescope = telescope,
                fixFront = fixFront,
                fixBack = fixBack
            )
        }
    }

    fun deleteAlarms() {
        repository.queueCommand(SendCommand.TextMessage("delete_logs"))
    }

    fun resetErrors() {
        _craneStatus.update { current ->
            current.copy(
                dirtyFilter = "0",
                highVoltage = "0",
                overWeight = "0",
                seatBelt = "0",
                overPressure = "0",
                badClutch = "0",
                greaseChange = "0",
                hydroOilChange = "0"
            )
        }
    }

    private suspend fun checkCriticalErrors(old: CraneStatus, new: CraneStatus) {
        if (old.dirtyFilter == "0" && new.dirtyFilter == "1") _notificationEvent.emit("خطا: فیلتر کثیف است!")
        if (old.overPressure == "0" && new.overPressure == "1") _notificationEvent.emit("خطا: فشار هیدرولیک بالاست!")
        if (old.overWeight == "0" && new.overWeight == "1") _notificationEvent.emit("خطا: وزن سبد بیش از حد مجاز!")
        if (old.seatBelt == "0" && new.seatBelt == "1") _notificationEvent.emit("خطا: کمربند ایمنی باز است!")
        if (old.highVoltage == "0" && new.highVoltage == "1") _notificationEvent.emit("خطا: ولتاژ بالا تشخیص داده شد!")
        if (old.badClutch == "0" && new.badClutch == "1") _notificationEvent.emit("خطا: کلاچ مشکل دارد!")
        if (old.greaseChange == "0" && new.greaseChange == "1") _notificationEvent.emit("یادآوری: نیاز به گریس کاری!")
        if (old.hydroOilChange == "0" && new.hydroOilChange == "1") _notificationEvent.emit("یادآوری: روغن هیدرولیک نیاز به تعویض دارد!")
    }
}