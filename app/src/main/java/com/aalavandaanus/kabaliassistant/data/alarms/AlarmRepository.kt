package com.aalavandaanus.kabaliassistant.data.alarms

import kotlinx.coroutines.flow.Flow

interface AlarmRepository {

    fun getAllAlarms(): Flow<List<Alarm>>

    suspend fun getAlarmById(
        id: Long
    ): Alarm?

    suspend fun insertAlarm(
        alarm: Alarm
    ): Long

    suspend fun updateAlarm(
        alarm: Alarm
    )

    suspend fun deleteAlarm(
        alarm: Alarm
    )
}
