
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

private val KabaliNavy = Color(0xFF0B1120)
private val KabaliSurface = Color(0xFF151F33)
private val KabaliSurfaceLight = Color(0xFF1D2A43)
private val KabaliCyan = Color(0xFF67E8F9)
private val KabaliPurple = Color(0xFFC4B5FD)
private val KabaliText = Color(0xFFF1F5F9)
private val KabaliMuted = Color(0xFF9CAFC8)

@Composable
fun HomeScreen(
    onOpenAlarms: () -> Unit = {},
    onOpenReminders: () -> Unit = {},
    onOpenListening: () -> Unit = {},
    onOpenBirthdays: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
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
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome header
        // Welcome header and account avatar
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

        // Assistant panel
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = KabaliSurface
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(94.dp)
                        .background(
                            color = KabaliSurfaceLight,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "K",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = KabaliCyan
                    )
                }

                Text(
                    text = "I'm Kabali",
                    style = MaterialTheme.typography.headlineSmall,
                    color = KabaliText,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Your personal assistant",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KabaliMuted
                )

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = Color(0xFF193B3B)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = KabaliCyan,
                                    shape = CircleShape
                                )
                        )

                        Text(
                            text = "Ready to help",
                            color = KabaliCyan,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Text(
                    text = "Open Listening to interact with Kabali",
                    color = KabaliMuted,
                    style = MaterialTheme.typography.bodySmall
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenListening),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF164E63)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎙️  Open Listening",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // Quick actions
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Quick actions",
                style = MaterialTheme.typography.titleLarge,
                color = KabaliText,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Your everyday essentials, all in one place.",
                style = MaterialTheme.typography.bodyMedium,
                color = KabaliMuted
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    emoji = "⏰",
                    title = "Alarms",
                    subtitle = "Wake up on time",
                    accent = KabaliCyan,
                    onClick = onOpenAlarms
                )

                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    emoji = "📅",
                    title = "Reminders",
                    subtitle = "Remember your tasks",
                    accent = KabaliPurple,
                    onClick = onOpenReminders
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🎂",
                    title = "Birthdays",
                    subtitle = "Celebrate your people",
                    accent = KabaliPurple,
                    onClick = onOpenBirthdays
                )

                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    emoji = "⚙️",
                    title = "Settings",
                    subtitle = "Personalize Kabali",
                    accent = KabaliCyan,
                    onClick = onOpenSettings
                )
            }
        }

        // Footer
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = KabaliSurface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "Small things, made simpler.",
                    color = KabaliText,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = "Kabali is here to help you manage your day.",
                    color = KabaliMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(148.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = KabaliSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
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

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = subtitle,
                    color = KabaliMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
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