package com.aalavandaanus.kabaliassistant

import com.aalavandaanus.kabaliassistant.core.scheduling.Recurrence
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduleCalculator
import org.junit.Assert
import org.junit.Test
import java.util.Calendar

class ScheduleCalculatorTest {

    @Test
    fun oneTimeEvent_returnsFutureTriggerTime() {
        val now = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 6,
            hour = 10,
            minute = 0
        ).timeInMillis

        val trigger = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 6,
            hour = 11,
            minute = 30
        ).timeInMillis

        val result =
            ScheduleCalculator.calculateNextTrigger(
                triggerAtMillis = trigger,
                recurrence = Recurrence.None,
                nowMillis = now
            )

        Assert.assertEquals(
            trigger,
            result
        )
    }

    @Test
    fun oneTimeEvent_throwsWhenTriggerIsInPast() {
        val now = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 6,
            hour = 11,
            minute = 0
        ).timeInMillis

        val trigger = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 6,
            hour = 10,
            minute = 0
        ).timeInMillis

        try {
            ScheduleCalculator.calculateNextTrigger(
                triggerAtMillis = trigger,
                recurrence = Recurrence.None,
                nowMillis = now
            )

            Assert.assertTrue(
                "Expected IllegalArgumentException",
                false
            )

        } catch (e: IllegalArgumentException) {
            // Expected.
        }
    }

    @Test
    fun dailyEvent_returnsTodayWhenTimeIsStillAhead() {
        val now = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 6,
            hour = 9,
            minute = 0
        ).timeInMillis

        val trigger = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 1,
            hour = 10,
            minute = 30
        ).timeInMillis

        val expected = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 6,
            hour = 10,
            minute = 30
        ).timeInMillis

        val result =
            ScheduleCalculator.calculateNextTrigger(
                triggerAtMillis = trigger,
                recurrence = Recurrence.Daily,
                nowMillis = now
            )

        Assert.assertEquals(
            expected,
            result
        )
    }

    @Test
    fun dailyEvent_movesToTomorrowWhenTimeHasPassed() {
        val now = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 6,
            hour = 11,
            minute = 0
        ).timeInMillis

        val trigger = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 1,
            hour = 10,
            minute = 30
        ).timeInMillis

        val expected = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 7,
            hour = 10,
            minute = 30
        ).timeInMillis

        val result =
            ScheduleCalculator.calculateNextTrigger(
                triggerAtMillis = trigger,
                recurrence = Recurrence.Daily,
                nowMillis = now
            )

        Assert.assertEquals(
            expected,
            result
        )
    }

    @Test
    fun weeklyEvent_returnsNextSelectedDay() {
        val now = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 6,
            hour = 9,
            minute = 0
        ).timeInMillis

        val trigger = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 1,
            hour = 10,
            minute = 30
        ).timeInMillis

        val expected = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 7,
            hour = 10,
            minute = 30
        ).timeInMillis

        val result =
            ScheduleCalculator.calculateNextTrigger(
                triggerAtMillis = trigger,
                recurrence = Recurrence.Weekly(
                    daysOfWeek = setOf(
                        Calendar.WEDNESDAY,
                        Calendar.FRIDAY
                    )
                ),
                nowMillis = now
            )

        Assert.assertEquals(
            expected,
            result
        )
    }

    @Test
    fun weeklyEvent_wrapsToNextWeek() {
        val now = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 9,
            hour = 12,
            minute = 0
        ).timeInMillis

        val trigger = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 1,
            hour = 10,
            minute = 30
        ).timeInMillis

        val expected = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 14,
            hour = 10,
            minute = 30
        ).timeInMillis

        val result =
            ScheduleCalculator.calculateNextTrigger(
                triggerAtMillis = trigger,
                recurrence = Recurrence.Weekly(
                    daysOfWeek = setOf(
                        Calendar.WEDNESDAY
                    )
                ),
                nowMillis = now
            )

        Assert.assertEquals(
            expected,
            result
        )
    }

    @Test
    fun weeklyEvent_rejectsEmptyDays() {
        val now = System.currentTimeMillis()

        try {
            ScheduleCalculator.calculateNextTrigger(
                triggerAtMillis = now + 60_000,
                recurrence = Recurrence.Weekly(
                    daysOfWeek = emptySet()
                ),
                nowMillis = now
            )

            Assert.assertTrue(
                "Expected IllegalArgumentException",
                false
            )

        } catch (e: IllegalArgumentException) {
            // Expected.
        }
    }

    @Test
    fun monthlyEvent_returnsCurrentMonthWhenDateIsAhead() {
        val now = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 6,
            hour = 9,
            minute = 0
        ).timeInMillis

        val trigger = calendar(
            year = 2026,
            month = Calendar.JANUARY,
            day = 15,
            hour = 10,
            minute = 30
        ).timeInMillis

        val expected = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 15,
            hour = 10,
            minute = 30
        ).timeInMillis

        val result =
            ScheduleCalculator.calculateNextTrigger(
                triggerAtMillis = trigger,
                recurrence = Recurrence.Monthly,
                nowMillis = now
            )

        Assert.assertEquals(
            expected,
            result
        )
    }

    @Test
    fun monthlyEvent_movesToNextMonthWhenDateHasPassed() {
        val now = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 20,
            hour = 9,
            minute = 0
        ).timeInMillis

        val trigger = calendar(
            year = 2026,
            month = Calendar.JANUARY,
            day = 15,
            hour = 10,
            minute = 30
        ).timeInMillis

        val expected = calendar(
            year = 2026,
            month = Calendar.NOVEMBER,
            day = 15,
            hour = 10,
            minute = 30
        ).timeInMillis

        val result =
            ScheduleCalculator.calculateNextTrigger(
                triggerAtMillis = trigger,
                recurrence = Recurrence.Monthly,
                nowMillis = now
            )

        Assert.assertEquals(
            expected,
            result
        )
    }

    @Test
    fun yearlyEvent_returnsCurrentYearWhenDateIsAhead() {
        val now = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 6,
            hour = 9,
            minute = 0
        ).timeInMillis

        val trigger = calendar(
            year = 2020,
            month = Calendar.DECEMBER,
            day = 25,
            hour = 10,
            minute = 30
        ).timeInMillis

        val expected = calendar(
            year = 2026,
            month = Calendar.DECEMBER,
            day = 25,
            hour = 10,
            minute = 30
        ).timeInMillis

        val result =
            ScheduleCalculator.calculateNextTrigger(
                triggerAtMillis = trigger,
                recurrence = Recurrence.Yearly,
                nowMillis = now
            )

        Assert.assertEquals(
            expected,
            result
        )
    }

    @Test
    fun yearlyEvent_movesToNextYearWhenDateHasPassed() {
        val now = calendar(
            year = 2026,
            month = Calendar.OCTOBER,
            day = 6,
            hour = 9,
            minute = 0
        ).timeInMillis

        val trigger = calendar(
            year = 2020,
            month = Calendar.JUNE,
            day = 15,
            hour = 10,
            minute = 30
        ).timeInMillis

        val expected = calendar(
            year = 2027,
            month = Calendar.JUNE,
            day = 15,
            hour = 10,
            minute = 30
        ).timeInMillis

        val result =
            ScheduleCalculator.calculateNextTrigger(
                triggerAtMillis = trigger,
                recurrence = Recurrence.Yearly,
                nowMillis = now
            )

        Assert.assertEquals(
            expected,
            result
        )
    }

    private fun calendar(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ): Calendar {

        return Calendar.getInstance().apply {
            clear()

            set(
                year,
                month,
                day,
                hour,
                minute,
                0
            )
        }
    }
}