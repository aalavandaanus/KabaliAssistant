
package com.aalavandaanus.kabaliassistant.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.layout.ColumnScope
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.text.intl.Locale
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.RowScope

private val KabaliNavy = Color(0xFF0B1120)
private val KabaliSurface = Color(0xFF151F33)
private val KabaliSurfaceLight = Color(0xFF1D2A43)
private val KabaliCyan = Color(0xFF67E8F9)
private val KabaliPurple = Color(0xFFC4B5FD)
private val KabaliText = Color(0xFFF1F5F9)
private val KabaliMuted = Color(0xFF9CAFC8)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenAlarms: () -> Unit = {},
    onOpenReminders: () -> Unit = {},
    onOpenListening: () -> Unit = {},
    onOpenBirthdays: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val dashboardState: HomeDashboardState by
    viewModel.dashboardState.collectAsState()
    val platformLocale = Locale.current.platformLocale

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KabaliNavy)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Welcome header and existing Google account avatar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$greeting 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    color = KabaliText,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "What can I help you with today?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = KabaliMuted
                )
            }

            HomeAccountAvatar()
        }


        // Kabali assistant card

        // Compact Kabali assistant card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = KabaliSurface
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Kabali identity and salutation — left
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = KabaliSurfaceLight,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "K",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = KabaliCyan
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "I'm Kabali",
                                style = MaterialTheme.typography.titleMedium,
                                color = KabaliText,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Your personal assistant",
                                style = MaterialTheme.typography.bodySmall,
                                color = KabaliMuted
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(
                                    color = KabaliCyan,
                                    shape = CircleShape
                                )
                        )

                        Text(
                            text = "Ready to help",
                            style = MaterialTheme.typography.labelSmall,
                            color = KabaliCyan
                        )
                    }
                }

                // Listening action — right
                Surface(
                    modifier = Modifier
                        .width(88.dp)
                        .height(76.dp)
                        .clickable(onClick = onOpenListening),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF164E63)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "🎙️",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            text = "Listen",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
// Reminders — full width
        DashboardInfoCard(
            title = "REMINDERS",
            emoji = "📝",
            accent = KabaliPurple,
            onClick = onOpenReminders
        ) {
            val reminder = dashboardState.upcomingReminder
            val overdue = dashboardState.overdueReminderCount

            if (reminder == null && overdue == 0) {
                Text(
                    text = "You're all caught up. No upcoming reminders.",
                    color = KabaliMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                if (reminder != null) {
                    Text(
                        text = reminder.title,
                        color = KabaliText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = SimpleDateFormat(
                            "EEE, MMM d · h:mm a",
                            platformLocale
                        ).format(Date(reminder.triggerAtMillis)),
                        color = KabaliPurple,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (overdue > 0) {
                    Text(
                        text = "$overdue overdue reminder${if (overdue == 1) "" else "s"}",
                        color = KabaliCyan,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

// Next Alarm + Upcoming Birthday — two columns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            DashboardInfoCard(
                modifier = Modifier.weight(1f),
                title = "NEXT ALARM",
                emoji = "⏰",
                accent = KabaliCyan,
                onClick = onOpenAlarms
            ) {
                val alarm = dashboardState.nextAlarm

                if (alarm == null) {
                    Text(
                        text = "No enabled alarms",
                        color = KabaliMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = alarm.label,
                        color = KabaliText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = SimpleDateFormat(
                            "EEE, MMM d",
                            platformLocale
                        ).format(Date(alarm.triggerAtMillis)),
                        color = KabaliCyan,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = SimpleDateFormat(
                            "h:mm a",
                            platformLocale
                        ).format(Date(alarm.triggerAtMillis)),
                        color = KabaliText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            DashboardInfoCard(
                modifier = Modifier.weight(1f),
                title = "BIRTHDAYS",
                emoji = "🎂",
                accent = KabaliPurple,
                onClick = onOpenBirthdays
            ) {
                val birthday = dashboardState.nearestBirthday

                if (birthday == null) {
                    Text(
                        text = "No birthdays added",
                        color = KabaliMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = birthday.name,
                        color = KabaliText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = when (birthday.daysUntilBirthday) {
                            0 -> "Today! 🎉"
                            1 -> "Tomorrow"
                            else -> "In ${birthday.daysUntilBirthday} days"
                        },
                        color = KabaliPurple,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = SimpleDateFormat(
                            "MMM d",
                            platformLocale
                        ).format(Date(birthday.nextBirthdayAtMillis)),
                        color = KabaliMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }


        // Compact quick actions
        Text(
            text = "Quick actions",
            style = MaterialTheme.typography.titleLarge,
            color = KabaliText,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                emoji = "⏰",
                title = "Alarms",
                accent = KabaliCyan,
                onClick = onOpenAlarms
            )

            QuickActionCard(
                modifier = Modifier.weight(1f),
                emoji = "📝",
                title = "Reminders",
                accent = KabaliPurple,
                onClick = onOpenReminders
            )

            QuickActionCard(
                modifier = Modifier.weight(1f),
                emoji = "🎂",
                title = "Birthdays",
                accent = KabaliCyan,
                onClick = onOpenBirthdays
            )

            QuickActionCard(
                modifier = Modifier.weight(1f),
                emoji = "🎙️",
                title = "Listen",
                accent = KabaliPurple,
                onClick = onOpenListening
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun DashboardInfoCard(
    title: String,
    emoji: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = KabaliSurface
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = KabaliSurfaceLight,
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 21.sp
                    )
                }

                Text(
                    text = title,
                    color = accent,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "›",
                    color = accent,
                    fontSize = 26.sp
                )
            }

            content()
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(92.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = KabaliSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 22.sp
            )

            Text(
                text = title,
                color = accent,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeAccountAvatar() {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val auth = remember { FirebaseAuth.getInstance() }
    val manager = remember(context) { GoogleSignInManager(context) }
    val scope = rememberCoroutineScope()

    var user by remember { mutableStateOf(auth.currentUser) }
    var isBusy by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener {
            user = it.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(KabaliSurfaceLight)
                .clickable {
                    if (isBusy) return@clickable

                    if (user != null) {
                        menuExpanded = true
                    } else {
                        val currentActivity = activity
                        if (currentActivity == null) {
                            errorMessage = "Unable to open Google sign-in right now."
                        } else {
                            scope.launch {
                                isBusy = true
                                try {
                                    manager.signIn(currentActivity)
                                } catch (e: Exception) {
                                    errorMessage = e.localizedMessage
                                        ?: "Google sign-in failed. Please try again."
                                } finally {
                                    isBusy = false
                                }
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(26.dp),
                    color = KabaliCyan,
                    strokeWidth = 2.dp
                )
            } else if (user != null) {
                Text(
                    text = user?.displayName
                        ?.firstOrNull()
                        ?.uppercase()
                        ?: user?.email
                            ?.firstOrNull()
                            ?.uppercase()
                        ?: "U",
                    color = KabaliCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                AsyncImage(
                    model = user?.photoUrl,
                    contentDescription = "Google profile photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Sign in with Google",
                    tint = KabaliCyan,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Text(
            text = user?.displayName ?: if (user != null) {
                user?.email ?: "Account"
            } else {
                "Sign in"
            },
            color = KabaliMuted,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Column {
                        Text(user?.displayName ?: "Google account")
                        Text(
                            text = user?.email.orEmpty(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                onClick = { menuExpanded = false }
            )

            DropdownMenuItem(
                text = { Text("Sign out") },
                onClick = {
                    menuExpanded = false
                    scope.launch {
                        isBusy = true
                        try {
                            manager.signOut()
                        } catch (e: Exception) {
                            errorMessage = e.localizedMessage
                                ?: "Unable to sign out completely."
                        } finally {
                            isBusy = false
                        }
                    }
                }
            )
        }
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            containerColor = KabaliSurface,
            titleContentColor = KabaliText,
            textContentColor = KabaliMuted,
            title = { Text("Kabali account") },
            text = { Text(errorMessage.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK", color = KabaliCyan)
                }
            }
        )
    }
}