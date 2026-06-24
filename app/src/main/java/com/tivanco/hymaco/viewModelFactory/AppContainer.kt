package com.tivanco.hymaco.viewModelFactory

import android.content.Context
import com.tivanco.hymaco.repository.UdpRepository
import com.tivanco.hymaco.room.AppDatabase
import com.tivanco.hymaco.room.dao.AlarmDao
import com.tivanco.hymaco.room.dao.LogDao
import com.tivanco.hymaco.room.dao.SystemLogDao
import com.tivanco.hymaco.utils.TelemetryParser

interface AppContainer {
    // VARIABLES
    val parser: TelemetryParser

    // DAOs
    val alarmDao: AlarmDao
    val appLogDao: LogDao
    val sysLogDao: SystemLogDao

    // REPOSITORIES
    val udpRepository: UdpRepository
}

class AppConstructor(private val context: Context): AppContainer {
    // PRIVATE VARIABLES
    private val database by lazy { AppDatabase.getDatabase(context) }

    // VARIABLES
    override val parser: TelemetryParser by lazy { TelemetryParser() }

    // DAOs
    override val alarmDao: AlarmDao by lazy { database.alarmDao() }
    override val appLogDao: LogDao by lazy { database.logDao() }
    override val sysLogDao: SystemLogDao by lazy { database.systemLogDao() }

    // REPOSITORIES
    override val udpRepository: UdpRepository by lazy { UdpRepository(context) }
}