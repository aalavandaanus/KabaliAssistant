package com.aalavandaanus.kabaliassistant.features.birthdays

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aalavandaanus.kabaliassistant.data.birthdays.Birthday

@Composable
fun BirthdayListScreen(
    viewModel: BirthdayViewModel,
    onAddBirthday: () -> Unit,
    onEditBirthday: (Long) -> Unit
) {
    val birthdays by viewModel.birthdays.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBirthday
            ) {
                Text("+")
            }
        }
    ) { paddingValues ->

        if (birthdays.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No birthdays yet.",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Tap + to add a birthday."
                )
            }

            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = birthdays,
                key = { it.id }
            ) { birthday ->

                BirthdayCard(
                    birthday = birthday,
                    onClick = {
                        onEditBirthday(birthday.id)
                    },
                    onEnabledChanged = { enabled ->
                        viewModel.setBirthdayEnabled(
                            birthday = birthday,
                            enabled = enabled
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun BirthdayCard(
    birthday: Birthday,
    onClick: () -> Unit,
    onEnabledChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                    text = birthday.name,
                    style = MaterialTheme.typography.titleMedium
                )

                if (birthday.relation.isNotBlank()) {
                    Text(
                        text = birthday.relation,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = formatBirthdayDate(birthday),
                    style = MaterialTheme.typography.bodyMedium
                )

                if (birthday.contactNumber.isNotBlank()) {
                    Text(
                        text = birthday.contactNumber,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Switch(
                checked = birthday.reminderEnabled,
                onCheckedChange = onEnabledChanged
            )
        }
    }
}

private fun formatBirthdayDate(
    birthday: Birthday
): String {
    val months = listOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    )

    val monthName =
        months.getOrNull(birthday.month - 1) ?: ""

    return if (birthday.birthYear != null) {
        "$monthName ${birthday.day}, ${birthday.birthYear}"
    } else {
        "$monthName ${birthday.day}"
    }
}