package com.aalavandaanus.kabaliassistant.data.alarms

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Query(
        "SELECT * FROM alarms ORDER BY hour, minute"
    )
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query(
        "SELECT * FROM alarms WHERE id = :id"
    )
    suspend fun getAlarmById(
        id: Long
    ): AlarmEntity?

    @Insert
    suspend fun insertAlarm(
        alarm: AlarmEntity
    ): Long

    @Update
    suspend fun updateAlarm(
        alarm: AlarmEntity
    )

    @Delete
    suspend fun deleteAlarm(
        alarm: AlarmEntity
    )
}