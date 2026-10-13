
package com.aalavandaanus.kabaliassistant.features.profile

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


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

private fun profilePreferences(context: Context) =
    context.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val preferences = remember(context) { profilePreferences(context) }

    var displayName by remember {
        mutableStateOf(preferences.getString(KEY_DISPLAY_NAME, "").orEmpty())
    }
    var language by remember {
        mutableStateOf(preferences.getString(KEY_LANGUAGE, "English") ?: "English")
    }
    var addressStyle by remember {
        mutableStateOf(
            preferences.getString(KEY_ADDRESS_STYLE, "Friendly") ?: "Friendly"
        )
    }

    var showNameDialog by remember { mutableStateOf(false) }
    var nameDraft by remember { mutableStateOf(displayName) }
    var selectionDialog by remember { mutableStateOf<String?>(null) }

    val avatarLetter = displayName.trim()
        .firstOrNull()
        ?.uppercaseChar()
        ?.toString() ?: "K"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KabaliNavy)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar and display name
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(KabaliSurfaceLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarLetter,
                    color = KabaliCyan,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = displayName.ifBlank { "Your name" },
                color = KabaliText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = {
                    nameDraft = displayName
                    showNameDialog = true
                }
            ) {
                Text("Edit profile", color = KabaliCyan)
            }
        }

        // Preferences
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Preferences",
                color = KabaliText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            PreferenceRow(
                label = "Default language",
                value = language,
                onClick = { selectionDialog = "language" }
            )

            PreferenceRow(
                label = "How Kabali addresses you",
                value = when (addressStyle) {
                    "Name" -> "Use my name"
                    "Friendly" -> "Friendly and natural"
                    else -> "No special form of address"
                },
                onClick = { selectionDialog = "address" }
            )
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            containerColor = KabaliSurface,
            titleContentColor = KabaliText,
            textContentColor = KabaliText,
            title = { Text("Edit profile") },
            text = {
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { nameDraft = it },
                    label = { Text("Display name") },
                    singleLine = true,
                    placeholder = { Text("Enter your name") },
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
                        preferences.edit()
                            .putString(KEY_DISPLAY_NAME, savedName)
                            .apply()
                        showNameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            displayName = ""
                            nameDraft = ""
                            preferences.edit()
                                .remove(KEY_DISPLAY_NAME)
                                .apply()
                            showNameDialog = false
                        }
                    ) {
                        Text("Clear")
                    }
                    TextButton(onClick = { showNameDialog = false }) {
                        Text("Cancel")
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
                    if (selectionDialog == "language") {
                        "Default language"
                    } else {
                        "How should Kabali address you?"
                    }
                )
            },
            text = {
                Column {
                    options.forEach { option ->
                        val selected = if (selectionDialog == "language") {
                            option == language
                        } else {
                            option == addressStyle
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectionDialog == "language") {
                                        language = option
                                        preferences.edit()
                                            .putString(KEY_LANGUAGE, option)
                                            .apply()
                                    } else {
                                        addressStyle = option
                                        preferences.edit()
                                            .putString(KEY_ADDRESS_STYLE, option)
                                            .apply()
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
                            if (selected) {
                                Text("✓", color = KabaliCyan)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectionDialog = null }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun PreferenceRow(
    label: String,
    value: String,
    onClick: () -> Unit
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