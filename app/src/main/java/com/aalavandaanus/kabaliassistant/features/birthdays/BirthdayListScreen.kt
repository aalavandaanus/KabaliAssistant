package com.aalavandaanus.kabaliassistant.features.birthdays

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aalavandaanus.kabaliassistant.data.birthdays.Birthday
import com.aalavandaanus.kabaliassistant.features.familytree.FamilyTreeData
import java.time.format.DateTimeFormatter

@Composable
fun BirthdayListScreen(viewModel: BirthdayViewModel, onAddBirthday: () -> Unit, onEditBirthday: (Long) -> Unit) {
    val birthdays by viewModel.birthdays.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val familyBirthdays = remember {
        FamilyTreeData.people.filter { it.birthDate != null }
            .sortedWith(compareBy({ it.birthDate!!.monthValue }, { it.birthDate!!.dayOfMonth }))
    }
    Scaffold(floatingActionButton = {
        if (selectedTab == 1) FloatingActionButton(onClick = onAddBirthday) { Text("+") }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Family") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Others") })
            }
            if (selectedTab == 0) {
                if (familyBirthdays.isEmpty()) EmptyState("No family birthdays are included yet.", "Add authoritative birth dates to FamilyTreeData.kt.")
                else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(familyBirthdays, key = { it.id }) { person ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                Text(person.displayName, style = MaterialTheme.typography.titleMedium)
                                Text(person.birthDate!!.format(DateTimeFormatter.ofPattern("MMMM d")), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            } else {
                if (birthdays.isEmpty()) EmptyState("No birthdays yet.", "Tap + to add a birthday.")
                else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(birthdays, key = { it.id }) { birthday ->
                        BirthdayCard(birthday, onClick = { onEditBirthday(birthday.id) }, onEnabledChanged = { enabled -> viewModel.setBirthdayEnabled(birthday, enabled) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(title: String, detail: String) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(detail)
    }
}

@Composable
private fun BirthdayCard(birthday: Birthday, onClick: () -> Unit, onEnabledChanged: (Boolean) -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(birthday.name, style = MaterialTheme.typography.titleMedium)
                if (birthday.relation.isNotBlank()) Text(birthday.relation, style = MaterialTheme.typography.bodyMedium)
                Text(formatBirthdayDate(birthday), style = MaterialTheme.typography.bodyMedium)
                if (birthday.contactNumber.isNotBlank()) Text(birthday.contactNumber, style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = birthday.reminderEnabled, onCheckedChange = onEnabledChanged)
        }
    }
}

private fun formatBirthdayDate(birthday: Birthday): String {
    val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val monthName = months.getOrNull(birthday.month - 1) ?: ""
    return if (birthday.birthYear != null) "$monthName ${birthday.day}, ${birthday.birthYear}" else "$monthName ${birthday.day}"
}
