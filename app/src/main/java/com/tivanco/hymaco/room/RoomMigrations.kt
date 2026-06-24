package com.tivanco.hymaco.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {

            // 1️⃣ ساخت جدول جدید با تمام ستون‌های Entity
            db.execSQL(
                """
            CREATE TABLE system_logs_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                message TEXT NOT NULL,
                status TEXT NOT NULL,
                timestamp TEXT NOT NULL
            )
            """.trimIndent()
            )

            // 2️⃣ کپی دیتا
            db.execSQL(
                """
            INSERT INTO system_logs_new (id, message, status, timestamp)
            SELECT 
                id,
                message,
                'INFO' AS status,
                CAST(timestamp AS TEXT)
            FROM system_logs
            """.trimIndent()
            )

            // 3️⃣ حذف جدول قدیمی
            db.execSQL("DROP TABLE system_logs")

            // 4️⃣ تغییر نام
            db.execSQL(
                "ALTER TABLE system_logs_new RENAME TO system_logs"
            )
        }
    }

    fun getAllMigrations() = arrayOf(
        MIGRATION_1_2
    )
}