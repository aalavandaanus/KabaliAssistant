package com.aalavandaanus.kabaliassistant.data.alarms

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomAlarmRepository(
    private val alarmDao: AlarmDao
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmDao
            .getAllAlarms()
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override suspend fun getAlarmById(
        id: Long
    ): Alarm? {
        return alarmDao
            .getAlarmById(id)
            ?.toDomain()
    }

    override suspend fun insertAlarm(
        alarm: Alarm
    ): Long {
        return alarmDao.insertAlarm(
            alarm.toEntity()
        )
    }

    override suspend fun updateAlarm(
        alarm: Alarm
    ) {
        alarmDao.updateAlarm(
            alarm.toEntity()
        )
    }

    override suspend fun deleteAlarm(
        alarm: Alarm
    ) {
        alarmDao.deleteAlarm(
            alarm.toEntity()
        )
    }

    private fun AlarmEntity.toDomain(): Alarm {
        val days =
            if (repeatDays.isBlank()) {
                emptySet()
            } else {
                repeatDays
                    .split(",")
                    .mapNotNull { it.toIntOrNull() }
                    .toSet()
            }

        return Alarm(
            id = id,
            hour = hour,
            minute = minute,
            label = label,
            repeatDays = days,
            vibrationEnabled = vibrationEnabled,
            enabled = enabled
        )
    }

    private fun Alarm.toEntity(): AlarmEntity {
        return AlarmEntity(
            id = id,
            hour = hour,
            minute = minute,
            label = label,
            repeatDays = repeatDays
                .sorted()
                .joinToString(","),
            vibrationEnabled = vibrationEnabled,
            enabled = enabled
        )
    }
}
