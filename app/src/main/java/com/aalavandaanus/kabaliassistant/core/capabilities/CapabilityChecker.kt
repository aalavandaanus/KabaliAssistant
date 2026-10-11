
package com.aalavandaanus.kabaliassistant.core.capabilities

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationChannel

enum class Capability {
    MICROPHONE,
    CONTACTS,
    NOTIFICATIONS,
    EXACT_ALARMS,
    SPEECH_RECOGNITION
}

enum class CapabilityStatus {
    READY,
    NEEDS_PERMISSION,
    NEEDS_SETTINGS,
    UNAVAILABLE
}

data class CapabilityResult(
    val capability: Capability,
    val status: CapabilityStatus,
    val reason: String
)

class CapabilityChecker(context: Context) {

    private val appContext = context.applicationContext

    fun checkAll(): List<CapabilityResult> =
        listOf(
            checkMicrophone(),
            checkContacts(),
            checkNotifications(),
            checkExactAlarms(),
            checkSpeechRecognition()
        )

    private fun checkMicrophone() = checkRuntimePermission(
        Capability.MICROPHONE,
        Manifest.permission.RECORD_AUDIO,
        "Microphone access is required for voice listening."
    )

    private fun checkContacts() = checkRuntimePermission(
        Capability.CONTACTS,
        Manifest.permission.READ_CONTACTS,
        "Contacts access is required to look up saved contacts."
    )

    private fun checkRuntimePermission(
        capability: Capability,
        permission: String,
        reason: String
    ): CapabilityResult {
        val granted = ContextCompat.checkSelfPermission(
            appContext,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        return CapabilityResult(
            capability = capability,
            status = if (granted) {
                CapabilityStatus.READY
            } else {
                CapabilityStatus.NEEDS_PERMISSION
            },
            reason = if (granted) {
                "Permission granted."
            } else {
                reason
            }
        )
    }

    private fun checkNotifications(): CapabilityResult {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return CapabilityResult(
                Capability.NOTIFICATIONS,
                CapabilityStatus.NEEDS_PERMISSION,
                "Allow notifications so Kabali can alert you."
            )
        }

        if (!NotificationManagerCompat.from(appContext)
                .areNotificationsEnabled()
        ) {
            return CapabilityResult(
                Capability.NOTIFICATIONS,
                CapabilityStatus.NEEDS_SETTINGS,
                "Notifications are disabled for Kabali in Android Settings."
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = appContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            val channelIds = listOf(
                NotificationChannel.ALARM_CHANNEL_ID,
                NotificationChannel.GENERAL_CHANNEL_ID
            )

            val disabledChannel = channelIds.any { id ->
                manager.getNotificationChannel(id)?.importance ==
                        NotificationManager.IMPORTANCE_NONE
            }

            if (disabledChannel) {
                return CapabilityResult(
                    Capability.NOTIFICATIONS,
                    CapabilityStatus.NEEDS_SETTINGS,
                    "One or more Kabali notification channels are disabled."
                )
            }
        }

        return CapabilityResult(
            Capability.NOTIFICATIONS,
            CapabilityStatus.READY,
            "Notifications are enabled."
        )
    }

    private fun checkExactAlarms(): CapabilityResult {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return CapabilityResult(
                Capability.EXACT_ALARMS,
                CapabilityStatus.READY,
                "Special exact-alarm access is not required on this Android version."
            )
        }

        val manager = appContext.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

        return if (manager.canScheduleExactAlarms()) {
            CapabilityResult(
                Capability.EXACT_ALARMS,
                CapabilityStatus.READY,
                "Exact-alarm access is enabled."
            )
        } else {
            CapabilityResult(
                Capability.EXACT_ALARMS,
                CapabilityStatus.NEEDS_SETTINGS,
                "Enable exact-alarm access in Android Settings."
            )
        }
    }

    private fun checkSpeechRecognition(): CapabilityResult {
        val available = SpeechRecognizer.isRecognitionAvailable(appContext)

        return CapabilityResult(
            capability = Capability.SPEECH_RECOGNITION,
            status = if (available) {
                CapabilityStatus.READY
            } else {
                CapabilityStatus.UNAVAILABLE
            },
            reason = if (available) {
                "A speech-recognition service is available. Offline support must be checked separately."
            } else {
                "No speech-recognition service is available on this device."
            }
        )
    }
}