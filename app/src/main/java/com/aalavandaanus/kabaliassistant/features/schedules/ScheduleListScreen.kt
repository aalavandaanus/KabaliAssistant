
package com.aalavandaanus.kabaliassistant.features.schedules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aalavandaanus.kabaliassistant.core.scheduling.Recurrence
import com.aalavandaanus.kabaliassistant.data.schedules.Schedule
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleListScreen(
    viewModel: ScheduleViewModel,
    onAddSchedule: () -> Unit,
    onEditSchedule: (Long) -> Unit,
    onManageCategories: () -> Unit
) {
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var selectedCategoryId by rememberSaveable {
        mutableStateOf(-1L)
    }

    val now = System.currentTimeMillis()

    val filteredSchedules = schedules.filter {
        selectedCategoryId == -1L || it.categoryId == selectedCategoryId
    }

    val activeSchedules = filteredSchedules.filter {
        it.enabled && !it.completed
    }

    val overdueSchedules = activeSchedules
        .filter {
            it.triggerAtMillis <= now &&
                    it.recurrence == Recurrence.None
        }
        .sortedBy { it.triggerAtMillis }

    val upcomingSchedules = activeSchedules
        .filter {
            it.triggerAtMillis > now ||
                    it.recurrence != Recurrence.None
        }
        .sortedBy { it.triggerAtMillis }

    val disabledSchedules = filteredSchedules
        .filter { !it.enabled && !it.completed }
        .sortedBy { it.triggerAtMillis }

    val completedSchedules = filteredSchedules
        .filter { it.completed }
        .sortedByDescending { it.lastCompletedAtMillis ?: 0L }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSchedule
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add schedule"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onClick = onManageCategories) {
                    Text("Manage")
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "all_categories") {
                    FilterChip(
                        selected = selectedCategoryId == -1L,
                        onClick = { selectedCategoryId = -1L },
                        label = { Text("All") }
                    )
                }

                items(categories, key = { it.id }) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { selectedCategoryId = category.id },
                        label = {
                            Text("${category.icon} ${category.name}")
                        }
                    )
                }
            }

            if (filteredSchedules.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Nothing scheduled yet",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = "Add a class, bill, EMI, or any reminder.",
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Text(
                        text = "Tap + to add your first schedule.",
                        modifier = Modifier.padding(top = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (overdueSchedules.isNotEmpty()) {
                        item(key = "overdue_header") {
                            ScheduleSectionHeader(
                                title = "Overdue",
                                count = overdueSchedules.size
                            )
                        }

                        items(
                            overdueSchedules,
                            key = { "overdue_${it.id}" }
                        ) { schedule ->
                            ScheduleCard(
                                schedule = schedule,
                                category = categories.firstOrNull {
                                    it.id == schedule.categoryId
                                },
                                onToggle = {
                                    viewModel.setScheduleEnabled(schedule, it)
                                },
                                onDone = {
                                    viewModel.markCompleted(schedule)
                                },
                                onEdit = {
                                    onEditSchedule(schedule.id)
                                }
                            )
                        }
                    }

                    if (upcomingSchedules.isNotEmpty()) {
                        item(key = "upcoming_header") {
                            ScheduleSectionHeader(
                                title = "Upcoming",
                                count = upcomingSchedules.size
                            )
                        }

                        items(
                            upcomingSchedules,
                            key = { "upcoming_${it.id}" }
                        ) { schedule ->
                            ScheduleCard(
                                schedule = schedule,
                                category = categories.firstOrNull {
                                    it.id == schedule.categoryId
                                },
                                onToggle = {
                                    viewModel.setScheduleEnabled(schedule, it)
                                },
                                onDone = {
                                    viewModel.markCompleted(schedule)
                                },
                                onEdit = {
                                    onEditSchedule(schedule.id)
                                }
                            )
                        }
                    }

                    if (disabledSchedules.isNotEmpty()) {
                        item(key = "disabled_header") {
                            ScheduleSectionHeader(
                                title = "Notifications off",
                                count = disabledSchedules.size
                            )
                        }

                        items(
                            disabledSchedules,
                            key = { "disabled_${it.id}" }
                        ) { schedule ->
                            ScheduleCard(
                                schedule = schedule,
                                category = categories.firstOrNull {
                                    it.id == schedule.categoryId
                                },
                                onToggle = {
                                    viewModel.setScheduleEnabled(schedule, it)
                                },
                                onDone = {
                                    viewModel.markCompleted(schedule)
                                },
                                onEdit = {
                                    onEditSchedule(schedule.id)
                                }
                            )
                        }
                    }

                    if (completedSchedules.isNotEmpty()) {
                        item(key = "completed_header") {
                            ScheduleSectionHeader(
                                title = "Completed",
                                count = completedSchedules.size
                            )
                        }

                        items(
                            completedSchedules,
                            key = { "completed_${it.id}" }
                        ) { schedule ->
                            ScheduleCard(
                                schedule = schedule,
                                category = categories.firstOrNull {
                                    it.id == schedule.categoryId
                                },
                                onToggle = {},
                                onDone = null,
                                onEdit = {
                                    onEditSchedule(schedule.id)
                                }
                            )
                        }
                    }

                    if (
                        overdueSchedules.isEmpty() &&
                        upcomingSchedules.isEmpty() &&
                        disabledSchedules.isEmpty() &&
                        completedSchedules.isEmpty()
                    ) {
                        item(key = "no_category_schedules") {
                            Text("No schedules in this category.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleSectionHeader(
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = count.toString(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ScheduleCard(
    schedule: Schedule,
    category: ScheduleCategory?,
    onToggle: (Boolean) -> Unit,
    onDone: (() -> Unit)?,
    onEdit: () -> Unit
) {
    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = schedule.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (schedule.message.isNotBlank()) {
                        Text(
                            text = schedule.message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (!schedule.completed) {
                    Switch(
                        checked = schedule.enabled,
                        onCheckedChange = onToggle
                    )
                }
            }

            Text(
                text = "${category?.icon ?: "📌"} ${category?.name ?: "General"}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = formatScheduleDateTime(schedule.triggerAtMillis),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = recurrenceLabel(schedule.recurrence),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (schedule.completed) {
                Text(
                    text = "Completed",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium
                )
            } else if (onDone != null) {
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}

private fun formatScheduleDateTime(millis: Long): String {
    return SimpleDateFormat(
        "EEE, MMM d, yyyy • h:mm a",
        Locale.getDefault()
    ).format(Date(millis))
}

private fun recurrenceLabel(recurrence: Recurrence): String {
    return when (recurrence) {
        Recurrence.None -> "One time"
        Recurrence.Daily -> "Repeats daily"

        is Recurrence.Weekly -> {
            val days = recurrence.daysOfWeek
                .sorted()
                .joinToString(", ") { day ->
                    when (day) {
                        1 -> "Sun"
                        2 -> "Mon"
                        3 -> "Tue"
                        4 -> "Wed"
                        5 -> "Thu"
                        6 -> "Fri"
                        7 -> "Sat"
                        else -> day.toString()
                    }
                }

            "Weekly: $days"
        }

        Recurrence.Monthly -> "Repeats monthly"
        Recurrence.Yearly -> "Repeats yearly"
    }
}