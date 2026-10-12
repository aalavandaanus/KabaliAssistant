package com.aalavandaanus.kabaliassistant.data.schedules

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomScheduleRepository(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    override fun getAllSchedules(): Flow<List<Schedule>> =
        scheduleDao.getAllSchedules().map { schedules ->
            schedules.map { it.toDomain() }
        }

    override suspend fun getScheduleById(id: Long): Schedule? =
        scheduleDao.getScheduleById(id)?.toDomain()

    override suspend fun insertSchedule(schedule: Schedule): Long =
        scheduleDao.insertSchedule(schedule.toEntity())

    override suspend fun updateSchedule(schedule: Schedule) {
        scheduleDao.updateSchedule(schedule.toEntity())
    }

    override suspend fun deleteSchedule(schedule: Schedule) {
        scheduleDao.deleteSchedule(schedule.toEntity())
    }
}