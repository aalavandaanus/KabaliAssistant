package com.aalavandaanus.kabaliassistant.core.scheduling

import java.util.Calendar

object ScheduleCalculator {

    fun calculateNextTrigger(
        triggerAtMillis: Long,
        recurrence: Recurrence,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        return when (recurrence) {
            Recurrence.None ->
                calculateOneTime(triggerAtMillis, nowMillis)

            Recurrence.Daily ->
                calculateDaily(triggerAtMillis, nowMillis)

            is Recurrence.Weekly ->
                calculateWeekly(
                    triggerAtMillis,
                    recurrence.daysOfWeek,
                    nowMillis
                )

            Recurrence.Monthly ->
                calculateMonthly(triggerAtMillis, nowMillis)

            Recurrence.Yearly ->
                calculateYearly(triggerAtMillis, nowMillis)
        }
    }

    private fun calculateOneTime(
        triggerAtMillis: Long,
        nowMillis: Long
    ): Long {
        if (triggerAtMillis > nowMillis) {
            return triggerAtMillis
        }

        throw IllegalArgumentException(
            "One-time event must have a future trigger time."
        )
    }

    private fun calculateDaily(
        triggerAtMillis: Long,
        nowMillis: Long
    ): Long {
        val trigger = Calendar.getInstance().apply {
            timeInMillis = triggerAtMillis
        }

        val candidate = Calendar.getInstance().apply {
            timeInMillis = nowMillis

            set(
                Calendar.HOUR_OF_DAY,
                trigger.get(Calendar.HOUR_OF_DAY)
            )
            set(
                Calendar.MINUTE,
                trigger.get(Calendar.MINUTE)
            )
            set(
                Calendar.SECOND,
                trigger.get(Calendar.SECOND)
            )
            set(
                Calendar.MILLISECOND,
                trigger.get(Calendar.MILLISECOND)
            )
        }

        if (candidate.timeInMillis <= nowMillis) {
            candidate.add(
                Calendar.DAY_OF_YEAR,
                1
            )
        }

        return candidate.timeInMillis
    }

    private fun calculateWeekly(
        triggerAtMillis: Long,
        daysOfWeek: Set<Int>,
        nowMillis: Long
    ): Long {
        require(daysOfWeek.isNotEmpty()) {
            "Weekly recurrence requires at least one day."
        }

        require(
            daysOfWeek.all {
                it in Calendar.SUNDAY..Calendar.SATURDAY
            }
        ) {
            "Weekly recurrence contains an invalid day."
        }

        val trigger = Calendar.getInstance().apply {
            timeInMillis = triggerAtMillis
        }

        for (daysAhead in 0..7) {
            val candidate = Calendar.getInstance().apply {
                timeInMillis = nowMillis

                add(
                    Calendar.DAY_OF_YEAR,
                    daysAhead
                )

                set(
                    Calendar.HOUR_OF_DAY,
                    trigger.get(Calendar.HOUR_OF_DAY)
                )
                set(
                    Calendar.MINUTE,
                    trigger.get(Calendar.MINUTE)
                )
                set(
                    Calendar.SECOND,
                    trigger.get(Calendar.SECOND)
                )
                set(
                    Calendar.MILLISECOND,
                    trigger.get(Calendar.MILLISECOND)
                )
            }

            if (
                candidate.get(Calendar.DAY_OF_WEEK) in daysOfWeek &&
                candidate.timeInMillis > nowMillis
            ) {
                return candidate.timeInMillis
            }
        }

        throw IllegalStateException(
            "Unable to calculate weekly recurrence."
        )
    }

    private fun calculateMonthly(
        triggerAtMillis: Long,
        nowMillis: Long
    ): Long {
        val trigger = Calendar.getInstance().apply {
            timeInMillis = triggerAtMillis
        }

        val candidate = Calendar.getInstance().apply {
            timeInMillis = nowMillis

            set(
                Calendar.DAY_OF_MONTH,
                trigger.get(Calendar.DAY_OF_MONTH)
            )
            set(
                Calendar.HOUR_OF_DAY,
                trigger.get(Calendar.HOUR_OF_DAY)
            )
            set(
                Calendar.MINUTE,
                trigger.get(Calendar.MINUTE)
            )
            set(
                Calendar.SECOND,
                trigger.get(Calendar.SECOND)
            )
            set(
                Calendar.MILLISECOND,
                trigger.get(Calendar.MILLISECOND)
            )
        }

        if (candidate.timeInMillis <= nowMillis) {
            candidate.add(
                Calendar.MONTH,
                1
            )
        }

        return candidate.timeInMillis
    }

    private fun calculateYearly(
        triggerAtMillis: Long,
        nowMillis: Long
    ): Long {
        val trigger = Calendar.getInstance().apply {
            timeInMillis = triggerAtMillis
        }

        val candidate = Calendar.getInstance().apply {
            timeInMillis = nowMillis

            set(
                Calendar.MONTH,
                trigger.get(Calendar.MONTH)
            )
            set(
                Calendar.DAY_OF_MONTH,
                trigger.get(Calendar.DAY_OF_MONTH)
            )
            set(
                Calendar.HOUR_OF_DAY,
                trigger.get(Calendar.HOUR_OF_DAY)
            )
            set(
                Calendar.MINUTE,
                trigger.get(Calendar.MINUTE)
            )
            set(
                Calendar.SECOND,
                trigger.get(Calendar.SECOND)
            )
            set(
                Calendar.MILLISECOND,
                trigger.get(Calendar.MILLISECOND)
            )
        }

        if (candidate.timeInMillis <= nowMillis) {
            candidate.add(
                Calendar.YEAR,
                1
            )
        }

        return candidate.timeInMillis
    }
}