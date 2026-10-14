
package com.aalavandaanus.kabaliassistant.features.profile

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LogIn
import androidx.compose.material.icons.filled.LogOut
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aalavandaanus.kabaliassistant.features.home.GoogleSignInManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

private val KabaliNavy = Color(0xFF0B1120)
private val KabaliSurface = Color(0xFF151F33)
private val KabaliSurfaceLight = Color(0xFF1D2A43)
private val KabaliCyan = Color(0xFF67E8F9)
private val KabaliPurple = Color(0xFFC4B5FD)
private val KabaliText = Color(0xFFF1F5F9)
private val KabaliMuted = Color(0xFF9CAFC8)

private const val PROFILE_PREFS = "kabali_profile"
private const val KEY_DISPLAY_NAME = "display_name"
private const val KEY_LANGUAGE = "language"
private const val KEY_ADDRESS_STYLE = "address_style"
private const val ACCOUNT_STATE_PREFS = "kabali_account_state"
private const val KEY_LAST_GOOGLE_UID = "last_google_uid"

private fun profilePreferences(context: Context) =
    context.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)

@Composable
fun ProfileScreen(
    onOpenBackupRestore: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivityForProfile() }
    val preferences = remember(context) { profilePreferences(context) }
    val accountState = remember(context) {
        context.getSharedPreferences(ACCOUNT_STATE_PREFS, Context.MODE_PRIVATE)
    }
    val auth = remember { FirebaseAuth.getInstance() }
    val signInManager = remember(context) { GoogleSignInManager(context) }
    val scope = rememberCoroutineScope()

    var user by remember { mutableStateOf(auth.currentUser) }
    var displayName by remember {
        mutableStateOf(preferences.getString(KEY_DISPLAY_NAME, "").orEmpty())
    }
    var language by remember {
        mutableStateOf(preferences.getString(KEY_LANGUAGE, "English") ?: "English")
    }
    var addressStyle by remember {
        mutableStateOf(preferences.getString(KEY_ADDRESS_STYLE, "Friendly") ?: "Friendly")
    }
    var showNameDialog by remember { mutableStateOf(false) }
    var nameDraft by remember { mutableStateOf(displayName) }
    var selectionDialog by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var accountPrompt by remember { mutableStateOf<AccountPrompt?>(null) }
    var checkedAccountUid by remember { mutableStateOf<String?>(null) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            user = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    /*
     * Only prompt when a signed-in account differs from the last account
     * recorded by this screen. This does not upload, download, or restore data.
     * The account marker is kept outside backup-managed preferences.
     */
    LaunchedEffect(user?.uid) {
        val currentUser = user
        val uid = currentUser?.uid
        if (uid != null && checkedAccountUid != uid) {
            checkedAccountUid = uid
            val lastUid = accountState.getString(KEY_LAST_GOOGLE_UID, null)
            if (lastUid == null) {
                accountPrompt = AccountPrompt.FirstSignIn(currentUser)
            } else if (lastUid != uid) {
                accountPrompt = AccountPrompt.DifferentAccount(currentUser)
            }
            accountState.edit().putString(KEY_LAST_GOOGLE_UID, uid).apply()
        }
    }

    val avatarLetter = (user?.displayName?.firstOrNull()
        ?: user?.email?.firstOrNull()
        ?: displayName.firstOrNull()
        ?: 'G').uppercaseChar().toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KabaliNavy)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Profile",
            color = KabaliText,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = KabaliSurface
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(KabaliSurfaceLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = avatarLetter,
                            color = KabaliCyan,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (user?.photoUrl != null) {
                            AsyncImage(
                                model = user?.photoUrl,
                                contentDescription = "Google profile photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = user?.displayName
                                ?: user?.email
                                ?: "Guest",
                            color = KabaliText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user?.email ?: "Not signed in",
                            color = KabaliMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (user == null) "Guest profile · saved on this device"
                            else "Google account connected",
                            color = KabaliCyan,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                HorizontalDivider(color = KabaliSurfaceLight)

                if (user == null) {
                    Button(
                        onClick = {
                            val currentActivity = activity
                            if (currentActivity == null) {
                                errorMessage = "Unable to open Google sign-in right now."
                            } else {
                                scope.launch {
                                    busy = true
                                    try {
                                        signInManager.signIn(currentActivity)
                                    } catch (error: Exception) {
                                        errorMessage = error.localizedMessage
                                            ?: "Google sign-in failed. Please try again."
                                    } finally {
                                        busy = false
                                    }
                                }
                            }
                        },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (busy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = KabaliText,
                                strokeWidth = 2.dp
                            )
                        } else {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.LogIn,
                                contentDescription = null
                            )
                        }
                        Text(
                            text = if (busy) "Connecting…" else "Sign in with Google",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Text(
                        text = "Signing in keeps your current local data. Backup and restore remain manual.",
                        color = KabaliMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    TextButton(
                        onClick = {
                            scope.launch {
                                busy = true
                                try {
                                    signInManager.signOut()
                                    errorMessage = null
                                } catch (error: Exception) {
                                    errorMessage = error.localizedMessage
                                        ?: "Unable to complete sign-out."
                                } finally {
                                    busy = false
                                }
                            }
                        },
                        enabled = !busy
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.LogOut,
                            contentDescription = null,
                            tint = KabaliPurple
                        )
                        Text(
                            "Sign out",
                            modifier = Modifier.padding(start = 8.dp),
                            color = KabaliPurple
                        )
                    }
                    Text(
                        text = "Signing out disconnects Google but keeps your local data and Drive backups.",
                        color = KabaliMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = KabaliSurface
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Kabali preferred name",
                    color = KabaliText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = displayName.ifBlank { "Not set · Kabali can use your Google name or Guest" },
                    color = KabaliMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = {
                        nameDraft = displayName
                        showNameDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (displayName.isBlank()) "Set preferred name" else "Edit preferred name")
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Personalization",
                color = KabaliText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            PreferenceRow(
                label = "Language",
                value = language,
                onClick = { selectionDialog = "language" },
                icon = { androidx.compose.material3.Icon(Icons.Default.Language, contentDescription = null, tint = KabaliCyan) }
            )
            PreferenceRow(
                label = "How Kabali addresses you",
                value = when (addressStyle) {
                    "Name" -> "Use my name"
                    "Friendly" -> "Friendly and natural"
                    else -> "No special form of address"
                },
                onClick = { selectionDialog = "address" },
                icon = { androidx.compose.material3.Icon(Icons.Default.Tune, contentDescription = null, tint = KabaliPurple) }
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = KabaliSurface
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = KabaliCyan
                    )
                    Text(
                        text = "Backup & Restore",
                        color = KabaliText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
                Text(
                    text = if (user == null) {
                        "Sign in to Google, then authorize Drive when you choose to back up or restore."
                    } else {
                        "Back up your Kabali data manually to Google Drive. Restoring requires confirmation."
                    },
                    color = KabaliMuted,
                    style = MaterialTheme.typography.bodySmall
                )
                Button(
                    onClick = onOpenBackupRestore,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null
                    )
                    Text("Open Backup & Restore", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            containerColor = KabaliSurface,
            titleContentColor = KabaliText,
            textContentColor = KabaliText,
            title = { Text("Kabali preferred name") },
            text = {
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { nameDraft = it },
                    label = { Text("What should Kabali call you?") },
                    singleLine = true,
                    placeholder = { Text("Enter a preferred name") },
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = KabaliText,
                        unfocusedTextColor = KabaliText,
                        focusedBorderColor = KabaliCyan,
                        unfocusedBorderColor = KabaliMuted,
                        focusedLabelColor = KabaliCyan,
                        unfocusedLabelColor = KabaliMuted,
                        cursorColor = KabaliCyan
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val savedName = nameDraft.trim()
                        displayName = savedName
                        preferences.edit().putString(KEY_DISPLAY_NAME, savedName).apply()
                        showNameDialog = false
                    }
                ) { Text("Save", color = KabaliCyan) }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            displayName = ""
                            nameDraft = ""
                            preferences.edit().remove(KEY_DISPLAY_NAME).apply()
                            showNameDialog = false
                        }
                    ) { Text("Clear", color = KabaliPurple) }
                    TextButton(onClick = { showNameDialog = false }) {
                        Text("Cancel", color = KabaliMuted)
                    }
                }
            }
        )
    }

    val options = when (selectionDialog) {
        "language" -> listOf("English", "Tamil", "Tanglish")
        "address" -> listOf("Name", "Friendly", "None")
        else -> emptyList()
    }

    if (selectionDialog != null) {
        AlertDialog(
            onDismissRequest = { selectionDialog = null },
            containerColor = KabaliSurface,
            titleContentColor = KabaliText,
            textContentColor = KabaliText,
            title = {
                Text(
                    if (selectionDialog == "language") "Default language"
                    else "How should Kabali address you?"
                )
            },
            text = {
                Column {
                    options.forEach { option ->
                        val selected = if (selectionDialog == "language") {
                            option == language
                        } else option == addressStyle

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectionDialog == "language") {
                                        language = option
                                        preferences.edit().putString(KEY_LANGUAGE, option).apply()
                                    } else {
                                        addressStyle = option
                                        preferences.edit().putString(KEY_ADDRESS_STYLE, option).apply()
                                    }
                                    selectionDialog = null
                                }
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = optionLabel(option, selectionDialog == "address"),
                                color = if (selected) KabaliCyan else KabaliText,
                                modifier = Modifier.weight(1f)
                            )
                            if (selected) Text("✓", color = KabaliCyan)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectionDialog = null }) {
                    Text("Done", color = KabaliCyan)
                }
            }
        )
    }

    accountPrompt?.let { prompt ->
        val isDifferentAccount = prompt is AccountPrompt.DifferentAccount
        AlertDialog(
            onDismissRequest = { accountPrompt = null },
            containerColor = KabaliSurface,
            titleContentColor = KabaliText,
            textContentColor = KabaliText,
            title = {
                Text(if (isDifferentAccount) "Different Google account" else "Google connected")
            },
            text = {
                Text(
                    if (isDifferentAccount) {
                        "Your local Kabali data has been kept. Would you like to check whether this Google account has a backup? Nothing will be restored unless you choose a backup and confirm."
                    } else {
                        "Your existing local preferences and data are preserved. Would you like to open Backup & Restore to back up your local Kabali data?"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        accountPrompt = null
                        onOpenBackupRestore()
                    }
                ) {
                    Text(if (isDifferentAccount) "Check backups" else "Back up now", color = KabaliCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { accountPrompt = null }) {
                    Text("Not now", color = KabaliMuted)
                }
            }
        )
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

private sealed interface AccountPrompt {
    data class FirstSignIn(val user: FirebaseUser) : AccountPrompt
    data class DifferentAccount(val user: FirebaseUser) : AccountPrompt
}

@Composable
private fun PreferenceRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = KabaliSurface
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = label,
                    color = KabaliMuted,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = value,
                    color = KabaliText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text("›", color = KabaliPurple, fontSize = 28.sp)
        }
    }
}

private fun optionLabel(option: String, isAddressOption: Boolean): String {
    if (!isAddressOption) return option
    return when (option) {
        "Name" -> "Use my name"
        "Friendly" -> "Friendly and natural"
        else -> "No special form of address"
    }
}

private tailrec fun Context.findActivityForProfile(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivityForProfile()
        else -> null
    }
