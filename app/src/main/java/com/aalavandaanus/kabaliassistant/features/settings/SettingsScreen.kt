
package com.aalavandaanus.kabaliassistant.features.settings

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aalavandaanus.kabaliassistant.data.settings.AppSettingsRepository
import com.aalavandaanus.kabaliassistant.features.backup.BackupRestoreScreen
import com.aalavandaanus.kabaliassistant.features.birthdays.BirthdayWorkScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private enum class SettingsSection {
    ASSISTANT,
    VOICE,
    NOTIFICATIONS,
    PRIVACY,
    ADVANCED
}

@Composable
fun SettingsScreen(
    settingsRepository: AppSettingsRepository,
    onBack: () -> Unit,
    onOpenCapabilitySetup: () -> Unit,
    openBackupRestoreOnLaunch: Boolean = false,
    onBackupRestoreLaunchHandled: () -> Unit = {}
) {
    val context = LocalContext.current

    var showBackupRestore by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(openBackupRestoreOnLaunch) {
        if (openBackupRestoreOnLaunch) {
            showBackupRestore = true
            onBackupRestoreLaunchHandled()
        }
    }

    if (showBackupRestore) {
        BackupRestoreScreen(
            onBack = {
                showBackupRestore = false
            }
        )
        return
    }

    var expandedSection by remember {
        mutableStateOf<SettingsSection?>(null)
    }

    var notificationHour by remember {
        mutableIntStateOf(
            settingsRepository.getBirthdayNotificationHour()
        )
    }

    var notificationMinute by remember {
        mutableIntStateOf(
            settingsRepository.getBirthdayNotificationMinute()
        )
    }

    fun formattedTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        return SimpleDateFormat(
            "hh:mm a",
            Locale.getDefault()
        ).format(calendar.time)
    }

    fun toggleSection(section: SettingsSection) {
        expandedSection =
            if (expandedSection == section) null else section
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Make your assistant work your way.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SettingsSectionCard(
            title = "Assistant",
            description = "Name, language and personality",
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null
                )
            },
            expanded = expandedSection == SettingsSection.ASSISTANT,
            onClick = {
                toggleSection(SettingsSection.ASSISTANT)
            }
        ) {
            SettingsInfoRow(
                title = "Assistant name",
                description = "Change the name shown in Kabali."
            )

            OutlinedButton(
                onClick = onOpenCapabilitySetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Kabali Setup")
            }

            SettingsInfoRow(
                title = "Personality and language",
                description = "More personalization options can be added here."
            )
        }

        SettingsSectionCard(
            title = "Voice & Interaction",
            description = "Wake word, listening and speech",
            icon = {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null
                )
            },
            expanded = expandedSection == SettingsSection.VOICE,
            onClick = {
                toggleSection(SettingsSection.VOICE)
            }
        ) {
            SettingsInfoRow(
                title = "Voice features",
                description = "Voice preferences will be available here as they are implemented."
            )

            OutlinedButton(
                onClick = onOpenCapabilitySetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Check Required Permissions")
            }
        }

        SettingsSectionCard(
            title = "Notifications & Schedules",
            description = "Birthday alerts and reminders",
            icon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null
                )
            },
            expanded = expandedSection == SettingsSection.NOTIFICATIONS,
            onClick = {
                toggleSection(SettingsSection.NOTIFICATIONS)
            }
        ) {
            Text(
                text = "Birthday Notifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = formattedTime(
                    notificationHour,
                    notificationMinute
                ),
                onValueChange = {},
                readOnly = true,
                label = {
                    Text("Notification Time")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            notificationHour = hourOfDay
                            notificationMinute = minute

                            settingsRepository
                                .setBirthdayNotificationTime(
                                    hourOfDay,
                                    minute
                                )

                            BirthdayWorkScheduler.reschedule(
                                context = context,
                                hour = hourOfDay,
                                minute = minute
                            )
                        },
                        notificationHour,
                        notificationMinute,
                        false
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Notification Time")
            }

            Text(
                text = "Birthday reminders will be sent at this time during the birthday reminder window.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            SettingsInfoRow(
                title = "Reminders and schedules",
                description = "Future schedule preferences can be added here without creating another screen."
            )
        }

        SettingsSectionCard(
            title = "Permissions & Privacy",
            description = "Permissions, account and local data",
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null
                )
            },
            expanded = expandedSection == SettingsSection.PRIVACY,
            onClick = {
                toggleSection(SettingsSection.PRIVACY)
            }
        ) {
            SettingsInfoRow(
                title = "App permissions",
                description = "Review microphone, contacts, notifications and alarm access."
            )

            OutlinedButton(
                onClick = onOpenCapabilitySetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Review Permissions")
            }

            SettingsInfoRow(
                title = "Account and data",
                description = "Account and data-management options can be added here when available."
            )
        }

        SettingsSectionCard(
            title = "Advanced & About",
            description = "Diagnostics and app information",
            icon = {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null
                )
            },
            expanded = expandedSection == SettingsSection.ADVANCED,
            onClick = {
                toggleSection(SettingsSection.ADVANCED)
            }
        ) {
            SettingsInfoRow(
                title = "Diagnostics",
                description = "Troubleshooting options can be added here when implemented."
            )

            SettingsInfoRow(
                title = "Kabali Assistant",
                description = "Personal assistant settings and preferences."
            )

            HorizontalDivider()

            SettingsInfoRow(
                title = "Kabali Backup & Restore",
                description = "Back up your Kabali data to your own Google Drive and restore one of the three latest versions."
            )

            OutlinedButton(
                onClick = {
                    showBackupRestore = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Backup & Restore")
            }
        }

        Spacer(modifier = Modifier.size(4.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    expanded: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        icon()
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.ExpandMore
                    } else {
                        Icons.Default.ChevronRight
                    },
                    contentDescription = if (expanded) {
                        "Collapse $title"
                    } else {
                        "Expand $title"
                    }
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider()
                    content()
                }
            }
        }
    }
}

@Composable
private fun SettingsInfoRow(
    title: String,
    description: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}