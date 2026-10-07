package com.aalavandaanus.kabaliassistant.core.scheduling

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aalavandaanus.kabaliassistant.KabaliApplication
import com.aalavandaanus.kabaliassistant.data.alarms.Alarm
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScheduleReceiverTest {

    private lateinit var context: Context
    private lateinit var application: KabaliApplication

    private var createdAlarmId: Long? = null

    @Before
    fun setUp() {
        context =
            ApplicationProvider.getApplicationContext()

        application =
            context.applicationContext
                    as KabaliApplication

        assumeTrue(
            "Notification permission is required.",
            hasNotificationPermission()
        )
    }

    @Test
    fun alarmEvent_createsNotification() =
        runBlocking {

            val alarm =
                Alarm(
                    hour = 8,
                    minute = 30,
                    label = "Test Alarm",
                    repeatDays = emptySet(),
                    vibrationEnabled = true,
                    enabled = true
                )

            val alarmId =
                application.alarmRepository
                    .insertAlarm(alarm)

            createdAlarmId = alarmId

            val intent =
                Intent(
                    context,
                    ScheduleReceiver::class.java
                ).apply {

                    putExtra(
                        ScheduleReceiver.EXTRA_EVENT_ID,
                        alarmId
                    )

                    putExtra(
                        ScheduleReceiver.EXTRA_EVENT_TYPE,
                        ScheduledEventType.ALARM.name
                    )
                }

            context.sendBroadcast(intent)

            val notificationManager =
                NotificationManagerCompat.from(
                    context
                )

            val notificationShown =
                waitForNotification(
                    notificationManager =
                        notificationManager,
                    notificationId =
                        alarmId.toInt()
                )

            assertTrue(
                "Alarm event should create a notification.",
                notificationShown
            )

            notificationManager.cancel(
                alarmId.toInt()
            )
        }

    private fun hasNotificationPermission(): Boolean {
        return if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.TIRAMISU
        ) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat
                .from(context)
                .areNotificationsEnabled()
        }
    }

    private fun waitForNotification(
        notificationManager:
        NotificationManagerCompat,
        notificationId: Int,
        timeoutMillis: Long = 5_000L
    ): Boolean {

        val startTime =
            System.currentTimeMillis()

        while (
            System.currentTimeMillis() - startTime <
            timeoutMillis
        ) {

            val activeNotifications =
                notificationManager
                    .activeNotifications

            if (
                activeNotifications.any {
                    it.id == notificationId
                }
            ) {
                return true
            }

            Thread.sleep(100)
        }

        return false
    }

    @After
    fun tearDown() {
        val alarmId =
            createdAlarmId
                ?: return

        runBlocking {
            application.alarmRepository
                .getAlarmById(alarmId)
                ?.let { alarm ->

                    application.alarmRepository
                        .deleteAlarm(alarm)
                }
        }

        NotificationManagerCompat
            .from(context)
            .cancel(
                alarmId.toInt()
            )
    }
}