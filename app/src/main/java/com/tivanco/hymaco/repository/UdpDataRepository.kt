package com.tivanco.hymaco.repository

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import android.net.Network

private const val TAG = "UdpDataSource"

class UdpDataRepository {

    private val udpPort = 8888
    var transmissionInterval = 50L

    private var socket: DatagramSocket? = null
    private var sourceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var listenJob: Job? = null
    private var senderJob: Job? = null

    private val sendMutex = Mutex()
    // صف دریافت بایت‌ها برای ارسال
    private val sendChannel = Channel<Pair<ByteArray, String>>(Channel.CONFLATED)

    // پیام‌هایی که از شبکه دریافت می‌شن رو می‌ندازه اینجا
    private val _receivedMessages = MutableSharedFlow<Pair<String, String>>(replay = 0)
    val receivedMessages: SharedFlow<Pair<String, String>> = _receivedMessages.asSharedFlow()

    private var currentLocalIp: String = ""

    fun start(network: Network, localIp: String) {
        currentLocalIp = localIp
        try {
            socket = DatagramSocket(udpPort).apply {
                broadcast = true
                reuseAddress = true
            }
            network.bindSocket(socket)
            
            startListening()
            startSending()
        } catch (e: Exception) {
            Log.e(TAG, "خطا در ساخت سوکت: ${e.message}")
            throw e // ارور رو پرت می‌کنیم بالا تا ریپازیتوری بفهمه
        }
    }

    fun stop() {
        listenJob?.cancel()
        senderJob?.cancel()
        socket?.close()
        socket = null
        // برای استفاده‌های بعدی یه اسکوپ جدید می‌سازیم
        sourceScope.cancel()
        sourceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    // ارسال دیتا رو می‌ذاره تو صف
    fun queueData(data: ByteArray, targetIp: String) {
        sourceScope.launch {
            sendChannel.send(Pair(data, targetIp))
        }
    }

    // --- توابع داخلی ---

    private fun startListening() {
        listenJob = sourceScope.launch {
            val buffer = ByteArray(1024)
            val packet = DatagramPacket(buffer, buffer.size)

            while (isActive && socket?.isClosed == false) {
                try {
                    socket?.receive(packet)
                    val senderIP = packet.address.hostAddress ?: "255.255.255.255"
                    
                    // حذف پیام‌های خودمون
                    if (senderIP == currentLocalIp) continue

                    val message = String(packet.data, 0, packet.length).replace(" ", "").trim()
                    
                    // شلیک پیام به بیرون
                    _receivedMessages.emit(Pair(message, senderIP))

                } catch (e: Exception) {
                    if (isActive) Log.e(TAG, "خطا در دریافت: ${e.message}")
                }
            }
        }
    }

    private fun startSending() {
        senderJob = sourceScope.launch {
            for ((dataToSend, targetIp) in sendChannel) {
                sendMutex.withLock {
                    try {
                        val address = InetAddress.getByName(targetIp)
                        val packet = DatagramPacket(dataToSend, dataToSend.size, address, udpPort)
                        
                        socket?.send(packet)
                        delay(transmissionInterval)
                    } catch (e: Exception) {
                        Log.e(TAG, "خطا در ارسال: ${e.message}")
                    }
                }
            }
        }
    }
}
