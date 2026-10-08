package com.aalavandaanus.kabaliassistant.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aalavandaanus.kabaliassistant.data.alarms.AlarmDao
import com.aalavandaanus.kabaliassistant.data.alarms.AlarmEntity
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayDao
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayEntity
import com.aalavandaanus.kabaliassistant.data.reminders.ReminderDao
import com.aalavandaanus.kabaliassistant.data.reminders.ReminderEntity

@Database(
    entities = [
        AlarmEntity::class,
        ReminderEntity::class,
        BirthdayEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class KabaliDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    abstract fun reminderDao(): ReminderDao

    abstract fun birthdayDao(): BirthdayDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(
                database: SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS reminders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        message TEXT NOT NULL,
                        triggerAtMillis INTEGER NOT NULL,
                        enabled INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {

            override fun migrate(
                database: SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS birthdays (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        relation TEXT NOT NULL,
                        contactNumber TEXT NOT NULL,
                        month INTEGER NOT NULL,
                        day INTEGER NOT NULL,
                        birthYear INTEGER,
                        reminderEnabled INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}