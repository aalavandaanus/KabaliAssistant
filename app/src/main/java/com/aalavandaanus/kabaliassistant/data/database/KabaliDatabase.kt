
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
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleCategoryDao
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleCategoryEntity
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleDao
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleEntity

@Database(
    entities = [
        AlarmEntity::class,
        ReminderEntity::class,
        BirthdayEntity::class,
        ScheduleCategoryEntity::class,
        ScheduleEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class KabaliDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    abstract fun reminderDao(): ReminderDao

    abstract fun birthdayDao(): BirthdayDao

    abstract fun scheduleCategoryDao(): ScheduleCategoryDao

    abstract fun scheduleDao(): ScheduleDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
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
            override fun migrate(database: SupportSQLiteDatabase) {
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    ALTER TABLE reminders
                    ADD COLUMN completedAtMillis INTEGER
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {

                // 1. Create categories first because schedules reference them.
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS schedule_categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL,
                        color INTEGER NOT NULL,
                        isBuiltIn INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_schedule_categories_name
                    ON schedule_categories(name)
                    """.trimIndent()
                )

                // 2. Seed built-in categories with stable IDs.
                database.execSQL(
                    """
                    INSERT OR IGNORE INTO schedule_categories
                    (id, name, icon, color, isBuiltIn, sortOrder)
                    VALUES
                    (1, 'General', '📌', 4284513675, 1, 0),
                    (2, 'Classes', '📚', 4282339765, 1, 1),
                    (3, 'Bills', '💳', 4283215696, 1, 2),
                    (4, 'EMIs', '🏦', 4294940672, 1, 3)
                    """.trimIndent()
                )

                // 3. Create schedules with their category foreign key.
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS schedules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        message TEXT NOT NULL,
                        categoryId INTEGER NOT NULL,
                        triggerAtMillis INTEGER NOT NULL,
                        recurrenceType TEXT NOT NULL,
                        repeatDays TEXT NOT NULL,
                        enabled INTEGER NOT NULL,
                        completed INTEGER NOT NULL,
                        lastCompletedAtMillis INTEGER,
                        FOREIGN KEY(categoryId)
                            REFERENCES schedule_categories(id)
                            ON UPDATE NO ACTION
                            ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_schedules_categoryId
                    ON schedules(categoryId)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_schedules_triggerAtMillis
                    ON schedules(triggerAtMillis)
                    """.trimIndent()
                )

                // 4. Copy existing reminders without deleting the old records.
                database.execSQL(
                    """
                    INSERT INTO schedules (
                        id,
                        title,
                        message,
                        categoryId,
                        triggerAtMillis,
                        recurrenceType,
                        repeatDays,
                        enabled,
                        completed,
                        lastCompletedAtMillis
                    )
                    SELECT
                        id,
                        title,
                        message,
                        1,
                        triggerAtMillis,
                        'NONE',
                        '',
                        enabled,
                        CASE
                            WHEN completedAtMillis IS NOT NULL THEN 1
                            ELSE 0
                        END,
                        completedAtMillis
                    FROM reminders
                    """.trimIndent()
                )
            }
        }
    }
}