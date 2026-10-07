package com.aalavandaanus.kabaliassistant.core.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aalavandaanus.kabaliassistant.KabaliApplication
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ScheduleReceiver : BroadcastReceiver() {

    companion object {

        const val EXTRA_EVENT_ID =
            "extra_scheduled_event_id"

        const val EXTRA_EVENT_TYPE =
            "extra_scheduled_event_type"

        private const val TAG =
            "KabaliScheduleReceiver"
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val eventId =
            intent.getLongExtra(
                EXTRA_EVENT_ID,
                -1L
            )

        if (eventId == -1L) {
            Log.e(
                TAG,
                "Received scheduled event with invalid ID."
            )
            return
        }

        val eventTypeName =
            intent.getStringExtra(
                EXTRA_EVENT_TYPE
            )

        if (eventTypeName == null) {
            Log.e(
                TAG,
                "Scheduled event has no event type. " +
                        "eventId=$eventId"
            )
            return
        }

        val eventType =
            try {
                ScheduledEventType.valueOf(
                    eventTypeName
                )
            } catch (e: IllegalArgumentException) {
                Log.e(
                    TAG,
                    "Unknown scheduled event type: " +
                            eventTypeName,
                    e
                )
                return
            }

        val pendingResult =
            goAsync()

        val application =
            context.applicationContext
                    as KabaliApplication

        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO
        ).launch {

            try {

                processEvent(
                    context =
                        context.applicationContext,
                    application = application,
                    eventId = eventId,
                    eventType = eventType
                )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Error processing scheduled event. " +
                            "eventId=$eventId, " +
                            "eventType=$eventType",
                    e
                )

            } finally {

                pendingResult.finish()
            }
        }
    }

    private suspend fun processEvent(
        context: Context,
        application: KabaliApplication,
        eventId: Long,
        eventType: ScheduledEventType
    ) {

        Log.d(
            TAG,
            "Processing scheduled event. " +
                    "eventId=$eventId, " +
                    "eventType=$eventType"
        )

        val handler =
            application
                .scheduledEventHandlerRegistry
                .getHandler(eventType)

        if (handler == null) {

            Log.e(
                TAG,
                "No handler registered for " +
                        "eventType=$eventType"
            )

            return
        }

        val result =
            handler.handle(
                context = context,
                eventId = eventId
            )

        result.notification?.let { notification ->

            NotificationHelper(
                context
            ).show(
                notification
            )
        }

        result.nextEvent?.let { nextEvent ->

            Log.d(
                TAG,
                "Scheduling next occurrence. " +
                        "eventId=${nextEvent.id}"
            )

            application.scheduler.schedule(
                ScheduleRequest(
                    event = nextEvent
                )
            )
        }
    }
}