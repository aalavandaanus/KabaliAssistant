
package com.aalavandaanus.kabaliassistant.features.backup

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aalavandaanus.kabaliassistant.KabaliApplication
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventType
import com.aalavandaanus.kabaliassistant.data.alarms.Alarm
import com.aalavandaanus.kabaliassistant.data.reminders.Reminder
import com.aalavandaanus.kabaliassistant.data.schedules.toDomain
import com.aalavandaanus.kabaliassistant.features.birthdays.BirthdayWorkScheduler
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

private sealed interface BackupAction {
    data object Create : BackupAction
    data object ListBackups : BackupAction
    data class Restore(val fileId: String) : BackupAction
}

@Composable
fun BackupRestoreScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context.findActivityForBackup()
    val application = context.applicationContext as KabaliApplication
    val repository = remember {
        DriveBackupRepository(context, application.database)
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var backups by remember {
        mutableStateOf<List<DriveBackupFile>>(emptyList())
    }
    var selectedBackupId by remember {
        mutableStateOf<String?>(null)
    }
    var showRestoreConfirmation by remember {
        mutableStateOf(false)
    }
    var busy by remember {
        mutableStateOf(false)
    }
    var pendingAction by remember {
        mutableStateOf<BackupAction?>(null)
    }
    var status by remember {
        mutableStateOf(
            "Authorize Google Drive to create or restore a backup in your own account."
        )
    }

    val authorizationClient = remember(activity) {
        activity?.let { Identity.getAuthorizationClient(it) }
    }

    /*
     * Display success feedback for exactly 4 seconds, then dismiss it.
     * Errors and progress remain visible in the status text.
     */
    fun showSuccessMessage(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
        }

    }

    fun performAction(accessToken: String, action: BackupAction) {
        scope.launch {
            busy = true

            status = when (action) {
                BackupAction.Create ->
                    "Creating and verifying your backup…"

                BackupAction.ListBackups ->
                    "Looking for Kabali backups…"

                is BackupAction.Restore ->
                    "Restoring the selected backup…"
            }

            try {
                when (action) {
                    BackupAction.Create -> {
                        backups = repository.createBackup(accessToken)
                        selectedBackupId = null

                        status =
                            "Backup completed and verified. The 3 newest successful backups are retained."

                        showSuccessMessage(
                            "Backup completed successfully."
                        )
                    }

                    BackupAction.ListBackups -> {
                        backups = repository.listBackups(accessToken)
                        selectedBackupId = backups.firstOrNull()?.id

                        status = if (backups.isEmpty()) {
                            "No Kabali backups were found in this Google Drive account."
                        } else {
                            "Choose a backup below. Restoring replaces current local Kabali data."
                        }
                    }

                    is BackupAction.Restore -> {
                        cancelCurrentScheduledEvents(application)

                        try {
                            repository.restoreBackup(
                                accessToken,
                                action.fileId
                            )
                            refreshSettingsAfterRestore(
                                context,
                                application
                            )
                            rescheduleRestoredEvents(application)
                        } catch (error: Exception) {
                            // If restore fails, try to reschedule the
                            // records currently present in the database.
                            runCatching {
                                rescheduleRestoredEvents(application)
                            }
                            throw error
                        }

                        status =
                            "Restore completed. Kabali's local records and scheduled events were refreshed."

                        showSuccessMessage(
                            "Restore completed successfully."
                        )

                        backups = repository.listBackups(accessToken)
                    }
                }
            } catch (error: Exception) {
                status = error.message
                    ?: "The Google Drive operation failed."
            } finally {
                busy = false
            }
        }
    }

    val authorizationLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val data = result.data
                        ?: error("Google authorization returned no result.")

                    val authorizationResult: AuthorizationResult =
                        authorizationClient?.getAuthorizationResultFromIntent(data)
                            ?: error("Google Drive authorization is unavailable.")

                    val accessToken = authorizationResult.accessToken
                        ?: error("Google Drive did not return an access token.")

                    val action = pendingAction
                        ?: BackupAction.ListBackups

                    pendingAction = null
                    performAction(accessToken, action)
                } catch (error: Exception) {
                    pendingAction = null
                    busy = false
                    status = error.message
                        ?: "Google Drive authorization failed."
                }
            } else {
                pendingAction = null
                busy = false
                status =
                    "Google Drive access was cancelled. Your local data was not changed."
            }
        }

    fun authorize(action: BackupAction) {
        val client = authorizationClient

        if (activity == null || client == null) {
            status =
                "Could not open Google authorization. Reopen this screen from the app."
            return
        }

        busy = true
        pendingAction = action

        val request = AuthorizationRequest.builder()
            .setRequestedScopes(
                listOf(
                    Scope(
                        "https://www.googleapis.com/auth/drive.file"
                    )
                )
            )
            .build()

        client.authorize(request)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    val pendingIntent = result.pendingIntent

                    if (pendingIntent == null) {
                        pendingAction = null
                        busy = false
                        status =
                            "Google requested authorization, but no approval screen was returned."
                    } else {
                        authorizationLauncher.launch(
                            IntentSenderRequest.Builder(
                                pendingIntent
                            ).build()
                        )
                    }
                } else {
                    val accessToken = result.accessToken

                    if (accessToken.isNullOrBlank()) {
                        pendingAction = null
                        busy = false
                        status =
                            "Google Drive did not return an access token."
                    } else {
                        pendingAction = null
                        performAction(accessToken, action)
                    }
                }
            }
            .addOnFailureListener { error ->
                pendingAction = null
                busy = false
                status = error.message
                    ?: "Google Drive authorization failed."
            }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Kabali Backup & Restore",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Backups are stored in a visible 'Kabali Backups' folder in the Google Drive account you authorize. This is separate from Firebase sign-in.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Included: alarms, birthdays, schedules/reminders, schedule categories, profile preferences and Kabali settings. Excluded: audio recordings, logs and cache.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Restore replaces the corresponding local Kabali data. Your Android Clock app alarms are not included.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )

            Button(
                onClick = {
                    authorize(BackupAction.Create)
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back Up Now")
            }

            OutlinedButton(
                onClick = {
                    authorize(BackupAction.ListBackups)
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Find Backups / Restore")
            }

            if (busy) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                    Text("Please wait…")
                }
            }

            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium
            )

            if (backups.isNotEmpty()) {
                Text(
                    text = "Available backups",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = backups,
                        key = { it.id }
                    ) { backup ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedBackupId = backup.id
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected =
                                        selectedBackupId == backup.id,
                                    onClick = {
                                        selectedBackupId = backup.id
                                    }
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = backup.name,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = formatBackupTime(
                                            backup.createdTime
                                        ),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        showRestoreConfirmation = true
                    },
                    enabled = !busy && selectedBackupId != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Restore Selected Backup")
                }
            }

            OutlinedButton(
                onClick = onBack,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Settings")
            }
        }
    }

    if (showRestoreConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmation = false
            },
            title = {
                Text("Replace local Kabali data?")
            },
            text = {
                Text(
                    "This replaces local Kabali alarms, birthdays, schedules/reminders, schedule categories, profile preferences and Kabali settings with the selected backup. Android Clock alarms are not changed. This action cannot be automatically undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val fileId = selectedBackupId
                        showRestoreConfirmation = false

                        if (fileId != null) {
                            authorize(
                                BackupAction.Restore(fileId)
                            )
                        }
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmation = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

private suspend fun refreshSettingsAfterRestore(
    context: Context,
    application: KabaliApplication
) {
    val preferences = context.getSharedPreferences(
        "kabali_assistant_settings",
        Context.MODE_PRIVATE
    )

    application.appSettingsRepository.setAssistantName(
        preferences.getString(
            "assistant_name",
            "Kabali"
        ) ?: "Kabali"
    )

    val hour = preferences.getInt(
        "birthday_notification_hour",
        9
    )
    val minute = preferences.getInt(
        "birthday_notification_minute",
        0
    )

    application.appSettingsRepository.setBirthdayNotificationTime(
        hour,
        minute
    )

    BirthdayWorkScheduler.reschedule(
        context,
        hour,
        minute
    )
}

private suspend fun cancelCurrentScheduledEvents(
    application: KabaliApplication
) {
    application.database.alarmDao()
        .getAllAlarms()
        .first()
        .forEach { alarm ->
            application.scheduler.cancel(
                ScheduledEventType.ALARM,
                alarm.id
            )
        }

    application.database.reminderDao()
        .getAllReminders()
        .first()
        .forEach { reminder ->
            application.scheduler.cancel(
                ScheduledEventType.REMINDER,
                reminder.id
            )
        }

    application.database.scheduleDao()
        .getAllSchedules()
        .first()
        .forEach { schedule ->
            application.scheduler.cancel(
                ScheduledEventType.SCHEDULE,
                schedule.id
            )
        }
}

private suspend fun rescheduleRestoredEvents(
    application: KabaliApplication
) {
    val alarms = application.database.alarmDao()
        .getAllAlarms()
        .first()

    alarms.forEach { entity ->
        application.alarmService.update(
            Alarm(
                id = entity.id,
                hour = entity.hour,
                minute = entity.minute,
                label = entity.label,
                repeatDays = entity.repeatDays
                    .split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .toSet(),
                vibrationEnabled = entity.vibrationEnabled,
                enabled = entity.enabled
            )
        )
    }

    val reminders = application.database.reminderDao()
        .getAllReminders()
        .first()

    reminders.forEach { entity ->
        application.reminderService.update(
            Reminder(
                id = entity.id,
                title = entity.title,
                message = entity.message,
                triggerAtMillis = entity.triggerAtMillis,
                enabled = entity.enabled,
                completedAtMillis = entity.completedAtMillis
            )
        )
    }

    val schedules = application.database.scheduleDao()
        .getAllSchedules()
        .first()

    schedules.forEach { entity ->
        application.scheduleService.update(entity.toDomain())
    }
}

private fun formatBackupTime(value: String): String =
    try {
        val instant = Instant.parse(value)

        SimpleDateFormat(
            "MMM d, yyyy h:mm a",
            Locale.getDefault()
        ).format(Date.from(instant))
    } catch (_: Exception) {
        value.ifBlank { "Date unavailable" }
    }

private fun Context.findActivityForBackup(): Activity? {
    var current: Context = this

    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }

    return current as? Activity
}