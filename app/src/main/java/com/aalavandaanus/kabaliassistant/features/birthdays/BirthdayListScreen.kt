package com.aalavandaanus.kabaliassistant.features.birthdays

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayDatabase
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayEntity
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.filled.Edit

@Composable
fun BirthdayListScreen(
    onAddBirthday: () -> Unit,
    onEditBirthday: (Int) -> Unit,
    onBack: () -> Unit
){
    val context = LocalContext.current

    val database = remember {
        BirthdayDatabase.getDatabase(context)
    }

    val birthdayDao = database.birthdayDao()

    val birthdays by birthdayDao
        .getAllBirthdays()
        .collectAsState(initial = emptyList())

    val sortedBirthdays = birthdays.sortedBy {
        it.getNextBirthday()
    }

    val coroutineScope = rememberCoroutineScope()

    var birthdayToDelete by remember {
        mutableStateOf<BirthdayEntity?>(null)
    }

    // --------------------------------------------------------
    // DELETE CONFIRMATION
    // --------------------------------------------------------

    birthdayToDelete?.let { birthday ->

        AlertDialog(
            onDismissRequest = {
                birthdayToDelete = null
            },

            title = {
                Text("Delete Birthday?")
            },

            text = {
                Text(
                    "Are you sure you want to delete " +
                            birthday.name +
                            "'s birthday?"
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        coroutineScope.launch {

                            birthdayDao.deleteBirthday(
                                birthday
                            )

                            birthdayToDelete = null
                        }
                    }
                ) {
                    Text("Delete")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        birthdayToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // --------------------------------------------------------
    // SCREEN
    // --------------------------------------------------------

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        if (birthdays.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),

                horizontalAlignment = Alignment.CenterHorizontally,

                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "No birthdays added yet"
                )

                Text(
                    text = "Tap + to add a birthday."
                )
            }

        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 96.dp
                    )
            ) {

                val nextBirthday = sortedBirthdays.first()

                NextBirthdayCard(
                    birthday = nextBirthday
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 16.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    items(
                        items = sortedBirthdays,
                        key = {
                            it.id
                        }
                    ) { birthday ->

                        BirthdayCard(
                            birthday = birthday,

                            onEdit = {
                                onEditBirthday(birthday.id)
                            },

                            onDelete = {
                                birthdayToDelete = birthday
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddBirthday,

            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Birthday"
            )
        }
    }
}

// ============================================================
// NEXT BIRTHDAY CARD
// ============================================================

@Composable
private fun NextBirthdayCard(
    birthday: BirthdayEntity
) {

    val daysUntil = birthday.getDaysUntilBirthday()

    val nextBirthday = birthday.getNextBirthday()

    Card(
        modifier = Modifier.fillMaxWidth(),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp),

            verticalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text = "🎂 Next Birthday"
            )

            Text(
                text = birthday.name
            )

            if (birthday.relation.isNotBlank()) {

                Text(
                    text = birthday.relation
                )
            }

            Text(
                text = when (daysUntil) {

                    0L ->
                        "🎉 Birthday is today!"

                    1L ->
                        "🎂 Tomorrow!"

                    else ->
                        "$daysUntil days remaining"
                }
            )

            Text(
                text = "Date: ${
                    String.format(
                        "%02d/%02d/%04d",
                        nextBirthday.monthValue,
                        nextBirthday.dayOfMonth,
                        nextBirthday.year
                    )
                }"
            )
        }
    }
}

// ============================================================
// BIRTHDAY CARD
// ============================================================

@Composable
private fun BirthdayCard(
    birthday: BirthdayEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    val age = birthday.getAge()

    val nextBirthday = birthday.getNextBirthday()

    val daysUntil = birthday.getDaysUntilBirthday()

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = birthday.name
                    )

                    if (birthday.relation.isNotBlank()) {

                        Text(
                            text = birthday.relation
                        )
                    }
                }

                IconButton(
                    onClick = onEdit
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit ${birthday.name}"
                    )
                }


                IconButton(
                    onClick = onDelete
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Delete,

                        contentDescription =
                            "Delete ${birthday.name}"
                    )
                }
            }

            Text(
                text = "Age: $age"
            )

            Text(
                text = "Birthday: ${
                    String.format(
                        "%02d/%02d",
                        birthday.month,
                        birthday.day
                    )
                }"
            )

            Text(
                text = "Next birthday: " +
                        "${nextBirthday.monthValue}/" +
                        "${nextBirthday.dayOfMonth}/" +
                        "${nextBirthday.year}"
            )

            Text(
                text = when (daysUntil) {

                    0L ->
                        "🎉 Birthday is today!"

                    1L ->
                        "🎂 Birthday is tomorrow!"

                    else ->
                        "⏳ $daysUntil days remaining"
                }
            )

            if (birthday.contactNumber.isNotBlank()) {

                Text(
                    text = "📱 ${birthday.contactNumber}"
                )
            }

            Text(
                text =
                    if (birthday.notificationEnabled) {

                        "🔔 Reminder: " +
                                "${birthday.notificationDaysBefore} day before"

                    } else {

                        "🔕 Reminder: Off"
                    }
            )
        }
    }
}