
package com.aalavandaanus.kabaliassistant.features.schedules

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aalavandaanus.kabaliassistant.core.scheduling.Recurrence
import com.aalavandaanus.kabaliassistant.data.schedules.Schedule
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleScreen(
    viewModel: ScheduleViewModel,
    categories: List<ScheduleCategory>,
    existingSchedule: Schedule? = null,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    onDeleted: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val initialCalendar = remember(existingSchedule?.id) {
        Calendar.getInstance().apply {
            timeInMillis = existingSchedule?.triggerAtMillis
                ?: (System.currentTimeMillis() + 60 * 60 * 1000L)
        }
    }

    var title by rememberSaveable(existingSchedule?.id) {
        mutableStateOf(existingSchedule?.title.orEmpty())
    }
    var message by rememberSaveable(existingSchedule?.id) {
        mutableStateOf(existingSchedule?.message.orEmpty())
    }
    var categoryId by rememberSaveable(existingSchedule?.id) {
        mutableStateOf(existingSchedule?.categoryId ?: categories.firstOrNull()?.id ?: 1L)
    }
    var triggerAtMillis by rememberSaveable(existingSchedule?.id) {
        mutableStateOf(existingSchedule?.triggerAtMillis ?: initialCalendar.timeInMillis)
    }
    var recurrenceName by rememberSaveable(existingSchedule?.id) {
        mutableStateOf(
            when (existingSchedule?.recurrence) {
                Recurrence.Daily -> "Daily"
                is Recurrence.Weekly -> "Weekly"
                Recurrence.Monthly -> "Monthly"
                Recurrence.Yearly -> "Yearly"
                else -> "One time"
            }
        )
    }
    var repeatDays by rememberSaveable(existingSchedule?.id) {
        mutableStateOf(
            (existingSchedule?.recurrence as? Recurrence.Weekly)
                ?.daysOfWeek ?: setOf(2, 3, 4, 5, 6)
        )
    }
    var enabled by rememberSaveable(existingSchedule?.id) {
        mutableStateOf(existingSchedule?.enabled ?: true)
    }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var recurrenceMenuExpanded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val calendar = remember(triggerAtMillis) {
        Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
    }
    val dateFormatter = remember {
        SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    }
    val timeFormatter = remember {
        SimpleDateFormat("h:mm a", Locale.getDefault())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = if (existingSchedule == null) "Add schedule" else "Edit schedule",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = categoryMenuExpanded,
            onExpandedChange = { categoryMenuExpanded = it }
        ) {
            OutlinedTextField(
                value = categories.firstOrNull { it.id == categoryId }
                    ?.let { "${it.icon} ${it.name}" }.orEmpty(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = categoryMenuExpanded
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = categoryMenuExpanded,
                onDismissRequest = { categoryMenuExpanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text("${category.icon} ${category.name}") },
                        onClick = {
                            categoryId = category.id
                            categoryMenuExpanded = false
                        }
                    )
                }
            }
        }

        Text("Date: ${dateFormatter.format(Date(triggerAtMillis))}")

        Button(
            onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val updated = Calendar.getInstance().apply {
                            timeInMillis = triggerAtMillis
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, day)
                        }
                        triggerAtMillis = updated.timeInMillis
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Choose date")
        }

        Text("Time: ${timeFormatter.format(Date(triggerAtMillis))}")

        Button(
            onClick = {
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val updated = Calendar.getInstance().apply {
                            timeInMillis = triggerAtMillis
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        triggerAtMillis = updated.timeInMillis
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Choose time")
        }

        ExposedDropdownMenuBox(
            expanded = recurrenceMenuExpanded,
            onExpandedChange = { recurrenceMenuExpanded = it }
        ) {
            OutlinedTextField(
                value = recurrenceName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Repeat") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = recurrenceMenuExpanded
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = recurrenceMenuExpanded,
                onDismissRequest = { recurrenceMenuExpanded = false }
            ) {
                listOf("One time", "Daily", "Weekly", "Monthly", "Yearly")
                    .forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                recurrenceName = option
                                recurrenceMenuExpanded = false
                            }
                        )
                    }
            }
        }

        if (recurrenceName == "Weekly") {
            Text("Repeat on", style = MaterialTheme.typography.titleMedium)

            val weekDays = listOf(
                1 to "Sun", 2 to "Mon", 3 to "Tue", 4 to "Wed",
                5 to "Thu", 6 to "Fri", 7 to "Sat"
            )

            weekDays.forEach { (day, label) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, modifier = Modifier.weight(1f))
                    Switch(
                        checked = day in repeatDays,
                        onCheckedChange = { checked ->
                            repeatDays = if (checked) {
                                repeatDays + day
                            } else {
                                repeatDays - day
                            }
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Notifications enabled", modifier = Modifier.weight(1f))
            Switch(
                checked = enabled,
                onCheckedChange = { enabled = it }
            )
        }

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                if (title.isBlank()) {
                    errorMessage = "Enter a title."
                    return@Button
                }
                if (categories.none { it.id == categoryId }) {
                    errorMessage = "Choose a category."
                    return@Button
                }
                if (recurrenceName == "Weekly" && repeatDays.isEmpty()) {
                    errorMessage = "Select at least one weekday."
                    return@Button
                }

                val recurrence = when (recurrenceName) {
                    "Daily" -> Recurrence.Daily
                    "Weekly" -> Recurrence.Weekly(repeatDays)
                    "Monthly" -> Recurrence.Monthly
                    "Yearly" -> Recurrence.Yearly
                    else -> Recurrence.None
                }

                val schedule = Schedule(
                    id = existingSchedule?.id ?: 0L,
                    title = title.trim(),
                    message = message.trim(),
                    categoryId = categoryId,
                    triggerAtMillis = triggerAtMillis,
                    recurrence = recurrence,
                    enabled = enabled,
                    completed = false,
                    lastCompletedAtMillis = existingSchedule?.lastCompletedAtMillis
                )

                saving = true
                val onResult: (Boolean) -> Unit = { success ->
                    saving = false
                    if (success) onSaved()
                    else errorMessage = "Could not save the schedule. Check the date and try again."
                }

                if (existingSchedule == null) {
                    viewModel.createSchedule(schedule, onResult)
                } else {
                    viewModel.updateSchedule(schedule, onResult)
                }
            },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (saving) "Saving..." else "Save schedule")
        }

        if (existingSchedule != null && onDeleted != null) {
            TextButton(
                onClick = {
                    saving = true
                    viewModel.deleteSchedule(existingSchedule) { success ->
                        saving = false
                        if (success) onDeleted()
                        else errorMessage = "Could not delete this schedule."
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete schedule")
            }
        }

        TextButton(
            onClick = onCancel,
            enabled = !saving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}