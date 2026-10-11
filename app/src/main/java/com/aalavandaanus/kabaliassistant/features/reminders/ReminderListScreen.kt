
package com.aalavandaanus.kabaliassistant.features.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aalavandaanus.kabaliassistant.data.reminders.Reminder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    viewModel: ReminderViewModel,
    onAddReminder: () -> Unit,
    onEditReminder: (Long) -> Unit
) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    LaunchedEffect(reminders) {
        android.util.Log.d(
            "ReminderListDebug",
            reminders.joinToString { reminder ->
                "id=${reminder.id}, completedAt=${reminder.completedAtMillis}"
            }
        )
    }
    val now = System.currentTimeMillis()

    val activeReminders = reminders.filter {
        it.completedAtMillis == null
    }

    val upcoming = activeReminders
        .filter { it.triggerAtMillis > now }
        .sortedBy { it.triggerAtMillis }

    val overdue = activeReminders
        .filter { it.triggerAtMillis <= now }
        .sortedBy { it.triggerAtMillis }

    val recentlyCompleted = reminders
        .filter { it.completedAtMillis != null }
        .sortedByDescending { it.completedAtMillis }
        .take(10)
    LaunchedEffect(reminders, now) {
        android.util.Log.d(
            "ReminderSectionsDebug",
            "Upcoming=${upcoming.map { it.id }}, " +
                    "Overdue=${overdue.map { it.id }}, " +
                    "Completed=${recentlyCompleted.map { it.id }}"
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Reminders") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddReminder) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add reminder"
                )
            }
        }
    ) { paddingValues ->
        if (reminders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No reminders yet",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Text("Tap + to create your first reminder.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (overdue.isNotEmpty()) {
                    item(key = "overdue_header") {
                        SectionHeader(
                            title = "Overdue",
                            count = overdue.size
                        )
                    }

                    items(overdue, key = { "overdue_${it.id}" }) { reminder ->
                        ReminderListItem(
                            reminder = reminder,
                            status = "Overdue",
                            onToggle = { enabled ->
                                viewModel.setReminderEnabled(reminder, enabled)
                            },
                            onDone = {
                                viewModel.markDone(reminder)
                            },
                            onUndo = null,
                            onClick = {
                                onEditReminder(reminder.id)
                            }
                        )
                    }
                }

                if (upcoming.isNotEmpty()) {
                    item(key = "upcoming_header") {
                        SectionHeader(
                            title = "Upcoming",
                            count = upcoming.size
                        )
                    }

                    items(upcoming, key = { "upcoming_${it.id}" }) { reminder ->
                        ReminderListItem(
                            reminder = reminder,
                            status = if (reminder.enabled) "Scheduled" else "Notifications off",
                            onToggle = { enabled ->
                                viewModel.setReminderEnabled(reminder, enabled)
                            },
                            onDone = {
                                viewModel.markDone(reminder)
                            },
                            onUndo = null,
                            onClick = {
                                onEditReminder(reminder.id)
                            }
                        )
                    }
                }

                if (recentlyCompleted.isNotEmpty()) {
                    item(key = "completed_header") {
                        SectionHeader(
                            title = "Recently completed",
                            count = recentlyCompleted.size
                        )
                    }

                    items(
                        recentlyCompleted,
                        key = { "completed_${it.id}" }
                    ) { reminder ->
                        ReminderListItem(
                            reminder = reminder,
                            status = "Completed",
                            onToggle = {},
                            onDone = null,
                            onUndo = {
                                viewModel.undoCompletion(reminder)
                            },
                            onClick = {
                                onEditReminder(reminder.id)
                            }
                        )
                    }
                }

                if (activeReminders.isEmpty() && recentlyCompleted.isEmpty()) {
                    item(key = "empty_reminders") {
                        Text("No active reminders.")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReminderListItem(
    reminder: Reminder,
    status: String,
    onToggle: (Boolean) -> Unit,
    onDone: (() -> Unit)?,
    onUndo: (() -> Unit)?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (reminder.message.isNotBlank()) {
                        Text(
                            text = reminder.message,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Text(
                        text = formatReminderDateTime(
                            reminder.triggerAtMillis
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (status == "Overdue") {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                if (reminder.completedAtMillis == null) {
                    Switch(
                        checked = reminder.enabled,
                        onCheckedChange = onToggle
                    )
                }
            }

            if (onDone != null) {
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }

            if (onUndo != null) {
                TextButton(
                    onClick = onUndo,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Undo")
                }
            }
        }
    }
}

private fun formatReminderDateTime(millis: Long): String {
    return SimpleDateFormat(
        "EEE, MMM d, yyyy • h:mm a",
        Locale.getDefault()
    ).format(Date(millis))
}