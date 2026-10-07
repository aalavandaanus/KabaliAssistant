package com.aalavandaanus.kabaliassistant.features.alarms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.aalavandaanus.kabaliassistant.data.alarms.Alarm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    viewModel: AlarmViewModel,
    onAddAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit
) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Alarms")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAlarm
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add alarm"
                )
            }
        }
    ) { paddingValues ->

        if (alarms.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No alarms yet",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(
                    modifier = Modifier.padding(8.dp)
                )

                Text("Tap + to create your first alarm.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = alarms,
                    key = { it.id }
                ) { alarm ->

                    AlarmListItem(
                        alarm = alarm,
                        onToggle = { enabled ->
                            viewModel.setAlarmEnabled(
                                alarm = alarm,
                                enabled = enabled
                            )
                        },
                        onClick = {
                            onEditAlarm(alarm.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmListItem(
    alarm: Alarm,
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
                    text = formatTime(
                        hour = alarm.hour,
                        minute = alarm.minute
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                if (alarm.label.isNotBlank()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Text(
                    text = formatRepeatDays(alarm.repeatDays),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Switch(
                checked = alarm.enabled,
                onCheckedChange = onToggle
            )
        }
    }
}

private fun formatTime(
    hour: Int,
    minute: Int
): String {
    val period = if (hour >= 12) "PM" else "AM"

    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }

    return String.format(
        "%d:%02d %s",
        displayHour,
        minute,
        period
    )
}

private fun formatRepeatDays(
    days: Set<Int>
): String {
    if (days.isEmpty()) {
        return "Once"
    }

    val names = mapOf(
        1 to "Sun",
        2 to "Mon",
        3 to "Tue",
        4 to "Wed",
        5 to "Thu",
        6 to "Fri",
        7 to "Sat"
    )

    return days
        .sorted()
        .mapNotNull { names[it] }
        .joinToString(" ")
}
