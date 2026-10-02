package com.aalavandaanus.kabaliassistant.features.birthdays

import android.annotation.SuppressLint
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayDatabase
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayEntity
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBirthdayScreen(
    birthdayId: Int? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val repository = remember {
        BirthdayRepository(
            BirthdayDatabase.getDatabase(context).birthdayDao()
        )
    }

    val scope = rememberCoroutineScope()

    var name by remember {
        mutableStateOf("")
    }

    var relation by remember {
        mutableStateOf("")
    }

    var contactNumber by remember {
        mutableStateOf("")
    }

    var birthday by remember {
        mutableStateOf("")
    }

    var selectedDateMillis by remember {
        mutableStateOf<Long?>(null)
    }

    var relationExpanded by remember {
        mutableStateOf(false)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var showValidationError by remember {
        mutableStateOf(false)
    }

    /*
     * Load the existing birthday when we are in Edit mode.
     */
    LaunchedEffect(birthdayId) {

        if (birthdayId != null) {

            val existingBirthday =
                repository.getBirthdayById(birthdayId)

            existingBirthday?.let { existing ->

                name = existing.name

                relation = existing.relation

                contactNumber = existing.contactNumber

                birthday = String.format(
                    "%02d/%02d/%04d",
                    existing.month,
                    existing.day,
                    existing.year
                )

                val calendar = Calendar.getInstance().apply {

                    set(
                        existing.year,
                        existing.month - 1,
                        existing.day,
                        0,
                        0,
                        0
                    )

                    set(
                        Calendar.MILLISECOND,
                        0
                    )
                }

                selectedDateMillis =
                    calendar.timeInMillis
            }
        }
    }

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

        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = if (birthdayId == null) {
                "Add Birthday"
            } else {
                "Edit Birthday"
            }
        )

        OutlinedTextField(
            value = name,

            onValueChange = {
                name = it
                showValidationError = false
            },

            label = {
                Text("Name")
            },

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

                label = {
                    Text("Relation")
                },

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
                        text = {
                            Text(item)
                        },

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

            label = {
                Text("Contact Number")
            },

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

            label = {
                Text("Birthday")
            },

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
                text =
                    "Please enter a name and select a birthday."
            )
        }

        Button(
            onClick = {

                if (
                    name.isBlank() ||
                    selectedDateMillis == null
                ) {

                    showValidationError = true

                    return@Button
                }

                val calendar =
                    Calendar.getInstance().apply {

                        timeInMillis =
                            selectedDateMillis!!
                    }

                scope.launch {

                    /*
                     * EDIT MODE
                     *
                     * First retrieve the existing record so
                     * notification settings are preserved.
                     */
                    if (birthdayId != null) {

                        val existingBirthday =
                            repository.getBirthdayById(
                                birthdayId
                            )

                        if (existingBirthday != null) {

                            val updatedBirthday =
                                existingBirthday.copy(

                                    name = name.trim(),

                                    relation = relation,

                                    contactNumber =
                                        contactNumber.trim(),

                                    month =
                                        calendar.get(
                                            Calendar.MONTH
                                        ) + 1,

                                    day =
                                        calendar.get(
                                            Calendar.DAY_OF_MONTH
                                        ),

                                    year =
                                        calendar.get(
                                            Calendar.YEAR
                                        )
                                )

                            repository.updateBirthday(
                                updatedBirthday
                            )
                        }

                    } else {

                        /*
                         * ADD MODE
                         */
                        val birthdayEntity =
                            BirthdayEntity(

                                name = name.trim(),

                                relation = relation,

                                contactNumber =
                                    contactNumber.trim(),

                                month =
                                    calendar.get(
                                        Calendar.MONTH
                                    ) + 1,

                                day =
                                    calendar.get(
                                        Calendar.DAY_OF_MONTH
                                    ),

                                year =
                                    calendar.get(
                                        Calendar.YEAR
                                    ),

                                notificationEnabled = true,

                                notificationDaysBefore = 1
                            )

                        repository.insertBirthday(
                            birthdayEntity
                        )
                    }

                    onBack()
                }
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                if (birthdayId == null) {
                    "Save Birthday"
                } else {
                    "Update Birthday"
                }
            )
        }

        TextButton(
            onClick = onBack,

            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Cancel")
        }
    }

    /*
     * DATE PICKER
     */
    if (showDatePicker) {

        val datePickerState =
            androidx.compose.material3.rememberDatePickerState(
                initialSelectedDateMillis =
                    selectedDateMillis
            )

        DatePickerDialog(

            onDismissRequest = {
                showDatePicker = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        datePickerState
                            .selectedDateMillis
                            ?.let { millis ->

                                selectedDateMillis =
                                    millis

                                birthday =
                                    SimpleDateFormat(
                                        "MM/dd/yyyy",
                                        Locale.getDefault()
                                    ).format(
                                        Date(millis)
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
