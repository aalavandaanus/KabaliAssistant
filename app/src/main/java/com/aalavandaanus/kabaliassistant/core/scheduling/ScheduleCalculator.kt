
package com.aalavandaanus.kabaliassistant.core.scheduling

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar

object ScheduleCalculator {

    fun calculateNextTrigger(
        triggerAtMillis: Long,
        recurrence: Recurrence,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        if (recurrence == Recurrence.None) {
            require(triggerAtMillis > nowMillis) {
                "One-time event must have a future trigger time."
            }
            return triggerAtMillis
        }

        // Preserve the chosen first occurrence for these patterns.
        // Weekly recurrence must also honor its selected weekdays.
        if (
            recurrence !is Recurrence.Weekly &&
            triggerAtMillis > nowMillis
        ) {
            return triggerAtMillis
        }

        val zone = ZoneId.systemDefault()
        val trigger = Instant.ofEpochMilli(triggerAtMillis).atZone(zone)
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)

        return when (recurrence) {
            Recurrence.None -> triggerAtMillis

            Recurrence.Daily -> {
                var candidate = ZonedDateTime.of(
                    now.toLocalDate(),
                    trigger.toLocalTime(),
                    zone
                )

                if (!candidate.isAfter(now)) {
                    candidate = candidate.plusDays(1)
                }

                candidate.toInstant().toEpochMilli()
            }

            is Recurrence.Weekly -> calculateWeekly(
                trigger = trigger,
                now = now,
                daysOfWeek = recurrence.daysOfWeek,
                zone = zone
            )

            Recurrence.Monthly -> calculateMonthly(
                trigger = trigger,
                now = now,
                zone = zone
            )

            Recurrence.Yearly -> calculateYearly(
                trigger = trigger,
                now = now,
                zone = zone
            )
        }
    }

    private fun calculateWeekly(
        trigger: ZonedDateTime,
        now: ZonedDateTime,
        daysOfWeek: Set<Int>,
        zone: ZoneId
    ): Long {
        require(daysOfWeek.isNotEmpty()) {
            "Weekly recurrence requires at least one day."
        }
        require(daysOfWeek.all {
            it in Calendar.SUNDAY..Calendar.SATURDAY
        }) {
            "Weekly recurrence contains an invalid day."
        }

        val triggerDate = trigger.toLocalDate()
        val nowDate = now.toLocalDate()
        val startDate = if (triggerDate.isAfter(nowDate)) {
            triggerDate
        } else {
            nowDate
        }

        for (daysAhead in 0..7) {
            val date = startDate.plusDays(daysAhead.toLong())
            val calendarDay = date.dayOfWeek.value % 7 + 1

            if (calendarDay !in daysOfWeek) continue

            val candidate = ZonedDateTime.of(
                date,
                trigger.toLocalTime(),
                zone
            )

            if (
                !candidate.isBefore(trigger) &&
                candidate.isAfter(now)
            ) {
                return candidate.toInstant().toEpochMilli()
            }
        }

        throw IllegalStateException("Unable to calculate weekly recurrence.")
    }

    private fun calculateMonthly(
        trigger: ZonedDateTime,
        now: ZonedDateTime,
        zone: ZoneId
    ): Long {
        val desiredDay = trigger.dayOfMonth
        val time = trigger.toLocalTime()
        var month = YearMonth.from(now)

        repeat(2) {
            val day = minOf(desiredDay, month.lengthOfMonth())
            val candidate = ZonedDateTime.of(
                month.atDay(day),
                time,
                zone
            )

            if (candidate.isAfter(now) && !candidate.isBefore(trigger)) {
                return candidate.toInstant().toEpochMilli()
            }

            month = month.plusMonths(1)
        }

        throw IllegalStateException("Unable to calculate monthly recurrence.")
    }

    private fun calculateYearly(
        trigger: ZonedDateTime,
        now: ZonedDateTime,
        zone: ZoneId
    ): Long {
        val month = trigger.month
        val desiredDay = trigger.dayOfMonth
        val time = trigger.toLocalTime()
        var year = now.year

        repeat(2) {
            val monthInYear = YearMonth.of(year, month)
            val day = minOf(desiredDay, monthInYear.lengthOfMonth())

            val candidate = ZonedDateTime.of(
                LocalDate.of(year, month, day),
                time,
                zone
            )

            if (candidate.isAfter(now) && !candidate.isBefore(trigger)) {
                return candidate.toInstant().toEpochMilli()
            }

            year++
        }

        throw IllegalStateException("Unable to calculate yearly recurrence.")
    }
}