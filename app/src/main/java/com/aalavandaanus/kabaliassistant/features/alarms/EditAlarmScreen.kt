package com.aalavandaanus.kabaliassistant.features.alarms

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.aalavandaanus.kabaliassistant.data.alarms.Alarm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAlarmScreen(
    alarm: Alarm,
    viewModel: AlarmViewModel,
    onSaved: () -> Unit,
    onDeleted: () -> Unit,
    onCancel: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = alarm.hour,
        initialMinute = alarm.minute,
        is24Hour = false
    )

    var label by remember(alarm.id) {
        mutableStateOf(alarm.label)
    }

    var selectedDays by remember(alarm.id) {
        mutableStateOf(alarm.repeatDays)
    }

    var vibrationEnabled by remember(alarm.id) {
        mutableStateOf(alarm.vibrationEnabled)
    }

    var enabled by remember(alarm.id) {
        mutableStateOf(alarm.enabled)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Edit Alarm")
                },
                navigationIcon = {
                    TextButton(
                        onClick = onCancel
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Text(
                text = "Alarm time",
                style = MaterialTheme.typography.titleMedium
            )

            TimePicker(
                state = timePickerState,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            OutlinedTextField(
                value = label,
                onValueChange = {
                    label = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Label")
                },
                singleLine = true
            )

            Text(
                text = "Repeat",
                style = MaterialTheme.typography.titleMedium
            )

            RepeatDaySelector(
                selectedDays = selectedDays,
                onSelectionChanged = {
                    selectedDays = it
                }
            )

            HorizontalDivider()

            SettingRow(
                title = "Vibration",
                checked = vibrationEnabled,
                onCheckedChange = {
                    vibrationEnabled = it
                }
            )

            SettingRow(
                title = "Enabled",
                checked = enabled,
                onCheckedChange = {
                    enabled = it
                }
            )

            Button(
                onClick = {
                    val updatedAlarm = alarm.copy(
                        hour = timePickerState.hour,
                        minute = timePickerState.minute,
                        label = label.trim(),
                        repeatDays = selectedDays,
                        vibrationEnabled = vibrationEnabled,
                        enabled = enabled
                    )

                    viewModel.updateAlarm(
                        alarm = updatedAlarm,
                        onResult = { success ->
                            if (success) {
                                onSaved()
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }

            TextButton(
                onClick = {
                    viewModel.deleteAlarm(
                        alarm = alarm,
                        onResult = onDeleted
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Delete Alarm",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
    }
}

@Composable
private fun RepeatDaySelector(
    selectedDays: Set<Int>,
    onSelectionChanged: (Set<Int>) -> Unit
) {
    val days = listOf(
        1 to "Sun",
        2 to "Mon",
        3 to "Tue",
        4 to "Wed",
        5 to "Thu",
        6 to "Fri",
        7 to "Sat"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEach { (day, name) ->

            val selected = day in selectedDays

            FilterChip(
                selected = selected,
                onClick = {
                    val updatedDays = selectedDays.toMutableSet()

                    if (selected) {
                        updatedDays.remove(day)
                    } else {
                        updatedDays.add(day)
                    }

                    onSelectionChanged(updatedDays)
                },
                label = {
                    Text(name)
                }
            )
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )

        Switch(
            checked = checked,
            onCheckedChange = null
        )
    }
}
