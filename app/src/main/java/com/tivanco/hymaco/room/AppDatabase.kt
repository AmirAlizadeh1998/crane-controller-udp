package com.tivanco.hymaco.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tivanco.hymaco.room.dao.AlarmDao
import com.tivanco.hymaco.room.dao.LogDao
import com.tivanco.hymaco.room.dao.SystemLogDao
import com.tivanco.hymaco.room.entities.AlarmEntity
import com.tivanco.hymaco.room.entities.AppLogEntity
import com.tivanco.hymaco.room.entities.SystemLogEntity

@Database(
    entities = [AppLogEntity::class, SystemLogEntity::class, AlarmEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
    abstract fun systemLogDao(): SystemLogDao
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "crane_log_db"
                )
//                    .addMigrations(*RoomMigrations.getAllMigrations())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
