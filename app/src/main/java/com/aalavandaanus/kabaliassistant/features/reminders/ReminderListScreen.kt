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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    viewModel: ReminderViewModel,
    onAddReminder: () -> Unit,
    onEditReminder: (Long) -> Unit
) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Reminders")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddReminder
            ) {
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

                items(
                    reminders,
                    key = { it.id }
                ) { reminder ->

                    ReminderListItem(
                        reminder = reminder,
                        onToggle = { enabled ->
                            viewModel.setReminderEnabled(
                                reminder = reminder,
                                enabled = enabled
                            )
                        },
                        onClick = {
                            onEditReminder(reminder.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderListItem(
    reminder: Reminder,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
            }

            Switch(
                checked = reminder.enabled,
                onCheckedChange = onToggle
            )
        }
    }
}

private fun formatReminderDateTime(
    millis: Long
): String {

    return SimpleDateFormat(
        "EEE, MMM d, yyyy • h:mm a",
        Locale.getDefault()
    ).format(Date(millis))
}