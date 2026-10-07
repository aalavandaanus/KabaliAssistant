package com.aalavandaanus.kabaliassistant.core.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmManagerScheduler(
    context: Context
) : Scheduler {

    companion object {
        private const val TAG =
            "KabaliAlarmManagerScheduler"
    }

    private val context =
        context.applicationContext

    private val alarmManager =
        context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

    override fun schedule(
        request: ScheduleRequest
    ): Boolean {

        val event = request.event

        Log.d(
            TAG,
            "schedule() called. " +
                    "eventId=${event.id}, " +
                    "type=${event.type}, " +
                    "triggerAtMillis=${event.triggerAtMillis}, " +
                    "enabled=${event.enabled}"
        )

        if (!event.enabled) {
            cancel(event.id)
            return false
        }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            Log.e(
                TAG,
                "Exact alarm permission is NOT granted."
            )

            return false
        }

        val triggerTime =
            ScheduleCalculator.calculateNextTrigger(
                triggerAtMillis =
                    event.triggerAtMillis,
                recurrence =
                    event.recurrence
            )

        val formattedTime =
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(
                Date(triggerTime)
            )

        Log.d(
            TAG,
            "Scheduling eventId=${event.id} " +
                    "at $formattedTime"
        )

        return try {

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                createPendingIntent(event)
            )

            Log.d(
                TAG,
                "Event successfully scheduled. " +
                        "eventId=${event.id}"
            )

            true

        } catch (e: SecurityException) {

            Log.e(
                TAG,
                "SecurityException while scheduling " +
                        "eventId=${event.id}",
                e
            )

            false
        }
    }

    override fun cancel(
        eventId: Long
    ) {

        Log.d(
            TAG,
            "Cancelling eventId=$eventId"
        )

        val pendingIntent =
            createPendingIntent(
                eventId = eventId
            )

        alarmManager.cancel(
            pendingIntent
        )

        pendingIntent.cancel()
    }

    private fun createPendingIntent(
        event: ScheduledEvent
    ): PendingIntent {

        val intent =
            Intent(
                context,
                ScheduleReceiver::class.java
            ).apply {

                putExtra(
                    ScheduleReceiver.EXTRA_EVENT_ID,
                    event.id
                )

                putExtra(
                    ScheduleReceiver.EXTRA_EVENT_TYPE,
                    event.type.name
                )
            }

        return PendingIntent.getBroadcast(
            context,
            event.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createPendingIntent(
        eventId: Long
    ): PendingIntent {

        val intent =
            Intent(
                context,
                ScheduleReceiver::class.java
            ).apply {

                putExtra(
                    ScheduleReceiver.EXTRA_EVENT_ID,
                    eventId
                )
            }

        return PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }
}