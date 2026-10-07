package com.aalavandaanus.kabaliassistant.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aalavandaanus.kabaliassistant.data.alarms.AlarmDao
import com.aalavandaanus.kabaliassistant.data.alarms.AlarmEntity

@Database(
    entities = [
        AlarmEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KabaliDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
}