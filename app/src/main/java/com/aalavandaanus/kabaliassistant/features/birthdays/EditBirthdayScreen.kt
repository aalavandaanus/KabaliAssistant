
package com.aalavandaanus.kabaliassistant.features.birthdays

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aalavandaanus.kabaliassistant.data.birthdays.Birthday
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBirthdayScreen(
    birthday: Birthday,
    viewModel: BirthdayViewModel,
    onSaved: () -> Unit,
    onDeleted: () -> Unit,
    onCancel: () -> Unit
) {
    var name by remember(birthday.id) {
        mutableStateOf(birthday.name)
    }

    var relation by remember(birthday.id) {
        mutableStateOf(birthday.relation)
    }

    var contactNumber by remember(birthday.id) {
        mutableStateOf(birthday.contactNumber)
    }

    var selectedDate by remember(birthday.id) {
        mutableStateOf(
            Calendar.getInstance().apply {
                set(Calendar.YEAR, birthday.birthYear ?: 2000)
                set(Calendar.MONTH, birthday.month - 1)
                set(Calendar.DAY_OF_MONTH, birthday.day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        )
    }

    var reminderEnabled by remember(birthday.id) {
        mutableStateOf(birthday.reminderEnabled)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var relationExpanded by remember {
        mutableStateOf(false)
    }

    val relationOptions = listOf(
        "Wife",
        "Husband",
        "Daughter",
        "Son",
        "Mother",
        "Father",
        "Sister",
        "Brother",
        "Friend",
        "Other"
    )

    val formattedDate =
        SimpleDateFormat(
            "dd MMMM yyyy",
            Locale.getDefault()
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(selectedDate.time)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Text(
            text = "Edit Birthday"
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
            },
            label = {
                Text("Name")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedTextField(
                value = relation,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text("Relation")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            DropdownMenu(
                expanded = relationExpanded,
                onDismissRequest = {
                    relationExpanded = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                relationOptions.forEach { option ->

                    DropdownMenuItem(
                        text = {
                            Text(option)
                        },
                        onClick = {
                            relation = option
                            relationExpanded = false
                        }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            OutlinedButton(
                onClick = {
                    relationExpanded = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Relation")
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = contactNumber,
            onValueChange = {
                contactNumber = it
            },
            label = {
                Text("Contact Number")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = formattedDate,
            onValueChange = {},
            readOnly = true,
            label = {
                Text("Birthday")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        OutlinedButton(
            onClick = {
                showDatePicker = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Change Birthday")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Birthday reminders"
                )

                Text(
                    text = "Remind me before the birthday"
                )
            }

            Switch(
                checked = reminderEnabled,
                onCheckedChange = {
                    reminderEnabled = it
                }
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = {

                    val updatedBirthday =
                        birthday.copy(
                            name = name.trim(),
                            relation = relation,
                            contactNumber = contactNumber.trim(),
                            month =
                                selectedDate.get(
                                    Calendar.MONTH
                                ) + 1,
                            day =
                                selectedDate.get(
                                    Calendar.DAY_OF_MONTH
                                ),
                            birthYear =
                                selectedDate.get(
                                    Calendar.YEAR
                                ),
                            reminderEnabled =
                                reminderEnabled
                        )

                    viewModel.updateBirthday(
                        birthday = updatedBirthday
                    ) { success ->

                        if (success) {
                            onSaved()
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Update")
            }

            OutlinedButton(
                onClick = {

                    viewModel.deleteBirthday(
                        birthday = birthday
                    ) {
                        onDeleted()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Delete")
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }

    if (showDatePicker) {

        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    calendarToDatePickerMillis(selectedDate)
            )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        datePickerState.selectedDateMillis
                            ?.let { millis ->

                                selectedDate =
                                    calendarFromDatePickerMillis(
                                        millis
                                    )
                            }

                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }
}

/**
 * Converts the birthday's local calendar date to the UTC-midnight
 * representation expected by Material3 DatePicker.
 */
private fun calendarToDatePickerMillis(
    calendar: Calendar
): Long {
    return Calendar.getInstance(
        TimeZone.getTimeZone("UTC")
    ).apply {
        set(Calendar.YEAR, calendar.get(Calendar.YEAR))
        set(Calendar.MONTH, calendar.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

/**
 * Converts Material3 DatePicker's UTC-midnight value into
 * a normal calendar containing the selected date.
 */
private fun calendarFromDatePickerMillis(
    millis: Long
): Calendar {
    return Calendar.getInstance(
        TimeZone.getTimeZone("UTC")
    ).apply {
        timeInMillis = millis
    }
}