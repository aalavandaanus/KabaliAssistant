package com.aalavandaanus.kabaliassistant.data.birthdays

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BirthdayEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BirthdayDatabase : RoomDatabase() {

    abstract fun birthdayDao(): BirthdayDao

    companion object {

        @Volatile
        private var INSTANCE: BirthdayDatabase? = null

        fun getDatabase(context: Context): BirthdayDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BirthdayDatabase::class.java,
                    "kabali_birthday_database"
                )
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}