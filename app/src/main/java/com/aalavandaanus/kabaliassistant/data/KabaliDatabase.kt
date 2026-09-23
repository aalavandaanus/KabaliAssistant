package com.aalavandaanus.kabaliassistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Birthday::class],
    version = 1,
    exportSchema = false
)
abstract class KabaliDatabase : RoomDatabase() {

    abstract fun birthdayDao(): BirthdayDao

    companion object {

        @Volatile
        private var INSTANCE: KabaliDatabase? = null

        fun getDatabase(context: Context): KabaliDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KabaliDatabase::class.java,
                    "kabali_assistant.db"
                ).build()

                INSTANCE = instance

                instance
            }
        }
    }
}