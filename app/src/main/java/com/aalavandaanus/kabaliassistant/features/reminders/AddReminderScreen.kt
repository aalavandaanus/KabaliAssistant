package com.aalavandaanus.kabaliassistant.features.reminders

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aalavandaanus.kabaliassistant.data.reminders.Reminder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    viewModel: ReminderViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    val initialCalendar = remember {
        Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    var title by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    var year by remember {
        mutableIntStateOf(initialCalendar.get(Calendar.YEAR))
    }

    var month by remember {
        mutableIntStateOf(initialCalendar.get(Calendar.MONTH))
    }

    var day by remember {
        mutableIntStateOf(initialCalendar.get(Calendar.DAY_OF_MONTH))
    }

    var hour by remember {
        mutableIntStateOf(initialCalendar.get(Calendar.HOUR_OF_DAY))
    }

    var minute by remember {
        mutableIntStateOf(initialCalendar.get(Calendar.MINUTE))
    }

    var enabled by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val selectedDateTime = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Add Reminder")
                },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Title")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = message,
                onValueChange = {
                    message = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Message")
                },
                minLines = 3
            )

            Text(
                text = "When",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, selectedYear, selectedMonth, selectedDay ->
                            year = selectedYear
                            month = selectedMonth
                            day = selectedDay
                            errorMessage = null
                        },
                        year,
                        month,
                        day
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    SimpleDateFormat(
                        "EEE, MMM d, yyyy",
                        Locale.getDefault()
                    ).format(selectedDateTime.time)
                )
            }

            Button(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, selectedHour, selectedMinute ->
                            hour = selectedHour
                            minute = selectedMinute
                            errorMessage = null
                        },
                        hour,
                        minute,
                        false
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    SimpleDateFormat(
                        "h:mm a",
                        Locale.getDefault()
                    ).format(selectedDateTime.time)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enabled",
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                    }
                )
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(
                modifier = Modifier.padding(4.dp)
            )

            Button(
                onClick = {

                    if (title.isBlank()) {
                        errorMessage = "Please enter a title."
                        return@Button
                    }

                    if (
                        enabled &&
                        selectedDateTime.timeInMillis <=
                        System.currentTimeMillis()
                    ) {
                        errorMessage =
                            "Please select a future date and time."
                        return@Button
                    }

                    val reminder = Reminder(
                        title = title.trim(),
                        message = message.trim(),
                        triggerAtMillis =
                            selectedDateTime.timeInMillis,
                        enabled = enabled
                    )

                    viewModel.createReminder(
                        reminder = reminder
                    ) { success ->

                        if (success) {
                            onSaved()
                        } else {
                            errorMessage =
                                "Unable to schedule the reminder."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Reminder")
            }
        }
    }
}