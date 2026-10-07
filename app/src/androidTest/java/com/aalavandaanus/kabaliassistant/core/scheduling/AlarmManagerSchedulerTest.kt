package com.aalavandaanus.kabaliassistant.core.scheduling

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class AlarmManagerSchedulerTest {

    private lateinit var context: Context
    private lateinit var scheduler: AlarmManagerScheduler
    private lateinit var alarmManager: AlarmManager

    @Before
    fun setUp() {
        context =
            ApplicationProvider.getApplicationContext()

        scheduler =
            AlarmManagerScheduler(context)

        alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager
    }

    @Test
    fun enabledEvent_schedulesSuccessfully() {

        assumeTrue(
            "Exact alarm permission is required.",
            canScheduleExactAlarms()
        )

        val event =
            ScheduledEvent(
                id = 1001L,
                type = ScheduledEventType.ALARM,
                triggerAtMillis =
                    futureTime(
                        minutesFromNow = 5
                    ),
                enabled = true,
                recurrence = Recurrence.None
            )

        val result =
            scheduler.schedule(
                ScheduleRequest(event)
            )

        assertTrue(
            "Enabled event should be scheduled successfully.",
            result
        )

        scheduler.cancel(event.id)
    }

    @Test
    fun disabledEvent_returnsFalse() {

        val event =
            ScheduledEvent(
                id = 1002L,
                type = ScheduledEventType.ALARM,
                triggerAtMillis =
                    futureTime(
                        minutesFromNow = 5
                    ),
                enabled = false,
                recurrence = Recurrence.None
            )

        val result =
            scheduler.schedule(
                ScheduleRequest(event)
            )

        assertFalse(
            "Disabled event should not be scheduled.",
            result
        )
    }

    @Test
    fun cancel_doesNotThrow() {

        val eventId = 1003L

        scheduler.cancel(eventId)
    }

    private fun canScheduleExactAlarms(): Boolean {
        return android.os.Build.VERSION.SDK_INT < 31 ||
                alarmManager.canScheduleExactAlarms()
    }

    private fun futureTime(
        minutesFromNow: Int
    ): Long {

        return Calendar.getInstance().apply {
            add(
                Calendar.MINUTE,
                minutesFromNow
            )
        }.timeInMillis
    }
}