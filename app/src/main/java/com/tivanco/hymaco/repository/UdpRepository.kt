package com.tivanco.hymaco.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.tivanco.hymaco.dataClass.ConnectionEvent
import com.tivanco.hymaco.dataClass.SendCommand
import com.tivanco.hymaco.dataClass.Status
import com.tivanco.hymaco.models.IncomingMessage
import com.tivanco.hymaco.models.UdpMessageParser
import com.tivanco.hymaco.utils.PrefsManager
import com.tivanco.hymaco.utils.SharedRepository
import com.tivanco.hymaco.utils.getCurrentJalaliDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.Inet4Address
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "CraneControl"

class UdpRepository(private val context: Context) {

    // ۱. اضافه شدن دیتاسورس برای کارهای کثیف شبکه!
    private val udpDataRepository = UdpDataRepository()

    private val _host = MutableStateFlow("255.255.255.255")
    val host: StateFlow<String> = _host.asStateFlow()

    private var connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var activeWifiNetwork: Network? = null

    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // دیگه نیازی به listenJob و senderJob نیست، رفتن تو DataSource
    private var handshakeJob: Job? = null
    private var disconnectJob: Job? = null
    private var messageObserverJob: Job? = null // برای گوش دادن به دیتاسورس

    // ==========================================
    // Flow ها (دقیقاً مثل قبل، بدون تغییر)
    // ==========================================
    private val _connectionStatus = MutableStateFlow(Status.DISCONNECTED)
    val connectionStatus: StateFlow<Status> = _connectionStatus.asStateFlow()

    private val _statusMessage = MutableStateFlow("در انتظار شروع...")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _lastConnectionTime = MutableStateFlow("00:00")
    val lastConnectionTime: StateFlow<String> = _lastConnectionTime.asStateFlow()

    private val _receivedMessages = MutableSharedFlow<String>(replay = 0)
    val receivedMessages: SharedFlow<String> = _receivedMessages.asSharedFlow()

    private val _connectionEvent = MutableSharedFlow<ConnectionEvent>()
    val connectionEvent: SharedFlow<ConnectionEvent> = _connectionEvent.asSharedFlow()

    // ==========================================
    // توابع اصلی
    // ==========================================

    fun connect() {
        if (_connectionStatus.value == Status.CONNECTED) return

        _connectionStatus.value = Status.CONNECTING
        _statusMessage.value = "در حال جستجوی شبکه وای‌فای..."

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                activeWifiNetwork = network
                _statusMessage.value = "وای‌فای پیدا شد، در حال راه‌اندازی سوکت..."
                startConnectionFlow(network) // تابع جدید
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                _statusMessage.value = "ارتباط وای‌فای قطع شد!"
                closeConnection()
            }
        }

        connectivityManager.requestNetwork(request, networkCallback!!)
    }

    fun disconnect() {
        if (_connectionStatus.value == Status.DISCONNECTED) return

        if (_connectionStatus.value == Status.CONNECTING) {
            closeConnection()
            return
        }

        _statusMessage.value = "در حال ارسال درخواست قطع ارتباط..."

        disconnectJob = repoScope.launch {
            try {
                queueCommand(SendCommand.TextMessage("disconnect_request"))

                delay(5000L) // تایم‌اوت ۵ ثانیه

                Log.d(TAG, "جواب disconnect_ok نیومد، تایم‌اوت ۵ ثانیه!")
                closeConnection()

            } catch (e: Exception) {
                if (e !is CancellationException) closeConnection()
            }
        }
    }

    private fun closeConnection() {
        // کنسل کردن جاب‌های منطقی
        handshakeJob?.cancel()
        disconnectJob?.cancel()
        messageObserverJob?.cancel()

        // خاموش کردن موتور سوکت
        udpDataRepository.stop()

        networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        networkCallback = null
        activeWifiNetwork = null
        _host.value = "255.255.255.255"

        _connectionStatus.value = Status.DISCONNECTED
        repoScope.launch { _connectionEvent.emit(ConnectionEvent.DISCONNECTED) }
        _statusMessage.value = "ارتباط قطع شد"
    }

    fun queueCommand(command: SendCommand) {
        // فقط دیتا و آی‌پی مقصد رو پاس میدیم به دیتاسورس
        udpDataRepository.queueData(command.data, _host.value)
    }

    // ==========================================
    // توابع داخلی (مدیریت منطق)
    // ==========================================

    private fun startConnectionFlow(network: Network) {
        try {
            val localIp = getWifiIpAddress()

            // ۱. روشن کردن دیتاسورس
            udpDataRepository.start(network, localIp)

            // ۲. شروع گوش دادن به پیام‌ها
            observeMessages()

            // ۳. شروع فرآیند هندشیک
            startHandshake(localIp)

        } catch (e: Exception) {
            _statusMessage.value = "خطا در راه‌اندازی شبکه: ${e.message}"
            closeConnection()
        }
    }

    private fun observeMessages() {
        messageObserverJob = repoScope.launch {
            udpDataRepository.receivedMessages.collect { (rawMessage, senderIP) ->
                Log.d(TAG, "received: $rawMessage from $senderIP")

                // ۱. استرینگ خام رو میدیم به پارسر تا مدل تمیز تحویل بگیریم
                val parsedMessage = UdpMessageParser.parse(rawMessage)

                // ۲. حالا ریپازیتوری فقط تصمیم می‌گیره (مدیریت جریان)
                when (parsedMessage) {
                    is IncomingMessage.ConnectOk -> {
                        if (_connectionStatus.value == Status.CONNECTING) {
                            PrefsManager.saveImei(context, parsedMessage.imei)

                            _host.value = senderIP // آی‌پی جرثقیل رو ذخیره می‌کنیم

                            repoScope.launch { _connectionEvent.emit(ConnectionEvent.CONNECTED) }
                            _connectionStatus.value = Status.CONNECTED
                            _statusMessage.value = "متصل شد"
                            _lastConnectionTime.value = getCurrentJalaliDate()

                            // هندشیک موفق بود، تایمر رو کنسل کن
                            handshakeJob?.cancel()
                        }
                    }

                    is IncomingMessage.DisconnectOk -> {
                        Log.d(TAG, "دستگاه با موفقیت ارتباط رو قطع کرد.")
                        closeConnection()
                    }

                    is IncomingMessage.StatusUpdate -> {
                        SharedRepository.updateDebugMsg(parsedMessage.rawMessage)
                        if (_connectionStatus.value == Status.CONNECTED) {
                            _receivedMessages.emit(parsedMessage.rawMessage)
                        }
                    }

                    is IncomingMessage.Generic -> {
                        // هر پیام دیگه‌ای که بود و متصل بودیم، می‌فرستیم واسه UI
                        if (_connectionStatus.value == Status.CONNECTED) {
                            _receivedMessages.emit(parsedMessage.rawMessage)
                        }
                    }
                }
            }
        }
    }

    private fun startHandshake(localIp: String) {
        _statusMessage.value = "سوکت ساخته شد، در حال ارسال درخواست اتصال..."

        handshakeJob = repoScope.launch {
            try {
                var attempts = 0
                val maxAttempts = 5

                // تا زمانی که وصل نشدیم و تلاش‌هامون تموم نشده، هی پیام رو بفرست
                while (_connectionStatus.value == Status.CONNECTING && attempts < maxAttempts) {
                    Log.d(TAG, "ارسال درخواست اتصال (تلاش ${attempts + 1} از $maxAttempts)...")

                    queueCommand(SendCommand.TextMessage("connect_request:$localIp"))

                    // 1 ثانیه صبر می‌کنیم تا جواب بیاد
                    delay(1000L)
                    attempts++
                }

                // اگه بعد از حلقه هنوز تو حالت CONNECTING بودیم، یعنی جواب نیومده
                if (_connectionStatus.value == Status.CONNECTING) {
                    _statusMessage.value = "دستگاه پیدا نشد (تایم‌اوت بعد از ۵ تلاش)"
                    closeConnection()
                }

            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _statusMessage.value = "خطا در ارسال درخواست: ${e.message}"
                    closeConnection()
                }
            }
        }
    }

    // --------------------- HELPERS -------------------------//
    private fun getWifiIpAddress(): String {
        val network = activeWifiNetwork ?: return ""
        val linkProperties = connectivityManager.getLinkProperties(network)

        linkProperties?.linkAddresses?.forEach { linkAddress ->
            val address = linkAddress.address
            if (!address.isLoopbackAddress && address is Inet4Address) {
                return address.hostAddress ?: ""
            }
        }
        return ""
    }
}