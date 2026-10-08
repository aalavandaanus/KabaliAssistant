package com.aalavandaanus.kabaliassistant.features.birthdays

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aalavandaanus.kabaliassistant.data.birthdays.Birthday
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBirthdayScreen(
    viewModel: BirthdayViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }

    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var relationExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }

    val relations = listOf(
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Add Birthday"
        )

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                showValidationError = false
            },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenuBox(
            expanded = relationExpanded,
            onExpandedChange = {
                relationExpanded = !relationExpanded
            }
        ) {
            OutlinedTextField(
                value = relation,
                onValueChange = {},
                readOnly = true,
                label = { Text("Relation") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = relationExpanded
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = relationExpanded,
                onDismissRequest = {
                    relationExpanded = false
                }
            ) {
                relations.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            relation = item
                            relationExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = contactNumber,
            onValueChange = {
                contactNumber = it
            },
            label = { Text("Contact Number") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = birthday,
            onValueChange = {},
            readOnly = true,
            label = { Text("Birthday") },
            placeholder = {
                Text("Select birthday")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                showDatePicker = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (birthday.isEmpty()) {
                    "Select Birthday"
                } else {
                    "Change Birthday"
                }
            )
        }

        if (showValidationError) {
            Text(
                text = "Please enter a name and select a birthday."
            )
        }

        Button(
            onClick = {
                if (name.isBlank() || selectedDateMillis == null) {
                    showValidationError = true
                    return@Button
                }

                val calendar = calendarFromDatePickerMillis(
                    selectedDateMillis!!
                )

                val newBirthday = Birthday(
                    name = name.trim(),
                    relation = relation,
                    contactNumber = contactNumber.trim(),
                    month = calendar.get(Calendar.MONTH) + 1,
                    day = calendar.get(Calendar.DAY_OF_MONTH),
                    birthYear = calendar.get(Calendar.YEAR),
                    reminderEnabled = true
                )

                viewModel.createBirthday(
                    birthday = newBirthday
                ) { success ->
                    if (success) {
                        onBack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Birthday")
        }

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }

    if (showDatePicker) {

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->

                            selectedDateMillis = millis

                            birthday = formatDatePickerMillis(
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
 * DatePicker selectedDateMillis represents the selected calendar
 * date at UTC midnight.
 *
 * Read the date components in UTC so the date does not shift
 * backward or forward because of the device time zone.
 */
private fun calendarFromDatePickerMillis(
    millis: Long
): Calendar {
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = millis
    }
}

/**
 * Formats the DatePicker value as the actual selected calendar date,
 * independent of the device time zone.
 */
private fun formatDatePickerMillis(
    millis: Long
): String {
    val formatter = SimpleDateFormat(
        "MM/dd/yyyy",
        Locale.getDefault()
    ).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    return formatter.format(millis)
}