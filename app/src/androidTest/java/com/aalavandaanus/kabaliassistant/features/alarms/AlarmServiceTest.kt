package com.aalavandaanus.kabaliassistant.features.alarms

import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduleRequest
import com.aalavandaanus.kabaliassistant.core.scheduling.Scheduler
import com.aalavandaanus.kabaliassistant.data.alarms.Alarm
import com.aalavandaanus.kabaliassistant.data.alarms.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AlarmServiceTest {

    private lateinit var repository: FakeAlarmRepository
    private lateinit var scheduler: FakeScheduler
    private lateinit var service: AlarmService

    @Before
    fun setUp() {
        repository = FakeAlarmRepository()
        scheduler = FakeScheduler()

        service =
            AlarmService(
                alarmRepository = repository,
                scheduler = scheduler
            )
    }

    @Test
    fun create_enabledAlarm_savesAndSchedules() =
        runBlocking {

            val alarm =
                testAlarm()

            val result =
                service.create(alarm)

            assertTrue(result)

            assertEquals(
                1,
                repository.insertedAlarms.size
            )

            assertEquals(
                1,
                scheduler.scheduledRequests.size
            )

            assertEquals(
                repository.insertedAlarms
                    .first()
                    .id,
                scheduler.scheduledRequests
                    .first()
                    .event
                    .id
            )
        }

    @Test
    fun create_disabledAlarm_savesButDoesNotSchedule() =
        runBlocking {

            val alarm =
                testAlarm(
                    enabled = false
                )

            val result =
                service.create(alarm)

            assertTrue(result)

            assertEquals(
                1,
                repository.insertedAlarms.size
            )

            assertEquals(
                0,
                scheduler.scheduledRequests.size
            )
        }

    @Test
    fun create_whenSchedulingFails_removesSavedAlarm() =
        runBlocking {

            scheduler.scheduleResult = false

            val alarm =
                testAlarm()

            val result =
                service.create(alarm)

            assertFalse(result)

            assertEquals(
                1,
                repository.insertedAlarms.size
            )

            assertEquals(
                1,
                repository.deletedAlarms.size
            )

            assertEquals(
                repository.insertedAlarms.first().id,
                repository.deletedAlarms.first().id
            )
        }

    @Test
    fun update_enabledAlarm_cancelsUpdatesAndSchedules() =
        runBlocking {

            val alarm =
                testAlarm(
                    id = 100L
                )

            val result =
                service.update(alarm)

            assertTrue(result)

            assertEquals(
                listOf(100L),
                scheduler.cancelledEventIds
            )

            assertEquals(
                listOf(alarm),
                repository.updatedAlarms
            )

            assertEquals(
                1,
                scheduler.scheduledRequests.size
            )
        }

    @Test
    fun update_disabledAlarm_cancelsAndDoesNotSchedule() =
        runBlocking {

            val alarm =
                testAlarm(
                    id = 101L,
                    enabled = false
                )

            val result =
                service.update(alarm)

            assertTrue(result)

            assertEquals(
                listOf(101L),
                scheduler.cancelledEventIds
            )

            assertEquals(
                listOf(alarm),
                repository.updatedAlarms
            )

            assertEquals(
                0,
                scheduler.scheduledRequests.size
            )
        }

    @Test
    fun setEnabled_false_updatesAndCancels() =
        runBlocking {

            val alarm =
                testAlarm(
                    id = 102L
                )

            val result =
                service.setEnabled(
                    alarm = alarm,
                    enabled = false
                )

            assertTrue(result)

            assertEquals(
                listOf(102L),
                scheduler.cancelledEventIds
            )

            assertEquals(
                false,
                repository.updatedAlarms
                    .single()
                    .enabled
            )

            assertEquals(
                0,
                scheduler.scheduledRequests.size
            )
        }

    @Test
    fun setEnabled_true_updatesAndSchedules() =
        runBlocking {

            val alarm =
                testAlarm(
                    id = 103L,
                    enabled = false
                )

            val result =
                service.setEnabled(
                    alarm = alarm,
                    enabled = true
                )

            assertTrue(result)

            assertEquals(
                true,
                repository.updatedAlarms
                    .single()
                    .enabled
            )

            assertEquals(
                1,
                scheduler.scheduledRequests.size
            )
        }

    @Test
    fun delete_cancelsAndDeletes() =
        runBlocking {

            val alarm =
                testAlarm(
                    id = 104L
                )

            service.delete(alarm)

            assertEquals(
                listOf(104L),
                scheduler.cancelledEventIds
            )

            assertEquals(
                listOf(alarm),
                repository.deletedAlarms
            )
        }

    private fun testAlarm(
        id: Long = 0L,
        enabled: Boolean = true
    ): Alarm {
        return Alarm(
            id = id,
            hour = 8,
            minute = 30,
            label = "Test Alarm",
            repeatDays = emptySet(),
            vibrationEnabled = true,
            enabled = enabled
        )
    }

    private class FakeAlarmRepository :
        AlarmRepository {

        val insertedAlarms =
            mutableListOf<Alarm>()

        val updatedAlarms =
            mutableListOf<Alarm>()

        val deletedAlarms =
            mutableListOf<Alarm>()

        private var nextId = 1L

        override fun getAllAlarms():
                Flow<List<Alarm>> =
            flowOf(emptyList())

        override suspend fun getAlarmById(
            id: Long
        ): Alarm? =
            insertedAlarms
                .find { it.id == id }

        override suspend fun insertAlarm(
            alarm: Alarm
        ): Long {

            val id = nextId++

            insertedAlarms.add(
                alarm.copy(id = id)
            )

            return id
        }

        override suspend fun updateAlarm(
            alarm: Alarm
        ) {
            updatedAlarms.add(alarm)
        }

        override suspend fun deleteAlarm(
            alarm: Alarm
        ) {
            deletedAlarms.add(alarm)
        }
    }

    private class FakeScheduler :
        Scheduler {

        var scheduleResult = true

        val scheduledRequests =
            mutableListOf<ScheduleRequest>()

        val cancelledEventIds =
            mutableListOf<Long>()

        override fun schedule(
            request: ScheduleRequest
        ): Boolean {

            scheduledRequests.add(
                request
            )

            return scheduleResult
        }

        override fun cancel(
            eventId: Long
        ) {
            cancelledEventIds.add(
                eventId
            )
        }
    }
}
