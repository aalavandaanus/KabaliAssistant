package com.aalavandaanus.kabaliassistant.features.schedules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleCategory

private data class CategoryColorOption(
    val name: String,
    val value: Long
)

private val categoryColors = listOf(
    CategoryColorOption("Blue", 0xFF4285F4),
    CategoryColorOption("Green", 0xFF34A853),
    CategoryColorOption("Red", 0xFFEA4335),
    CategoryColorOption("Orange", 0xFFFF9800),
    CategoryColorOption("Purple", 0xFF9C27B0),
    CategoryColorOption("Teal", 0xFF009688),
    CategoryColorOption("Pink", 0xFFE91E63),
    CategoryColorOption("Grey", 0xFF607D8B)
)

private val categoryIcons = listOf(
    "📌", "📚", "💳", "🏦", "💼",
    "🏠", "🩺", "🛒", "🎯", "🚗",
    "💡", "🎓", "📅", "⭐", "🔔"
)

@Composable
fun ManageScheduleCategoriesScreen(
    viewModel: ScheduleViewModel,
    categories: List<ScheduleCategory>,
    onBack: () -> Unit
) {
    var editingCategory by remember {
        mutableStateOf<ScheduleCategory?>(null)
    }
    var showEditor by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember {
        mutableStateOf<ScheduleCategory?>(null)
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.headlineSmall
            )

            TextButton(onClick = onBack) {
                Text("Back")
            }
        }

        Text(
            text = "Categories are labels for your schedules. " +
                    "They don't change reminder or recurrence behavior.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Button(
            onClick = {
                editingCategory = null
                errorMessage = null
                showEditor = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add category")
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories, key = { it.id }) { category ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = category.icon,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                                .background(
                                    color = Color(category.color),
                                    shape = CircleShape
                                )
                                .padding(10.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (category.isBuiltIn) {
                                    "Built-in category"
                                } else {
                                    "Custom category"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        TextButton(
                            onClick = {
                                editingCategory = category
                                errorMessage = null
                                showEditor = true
                            }
                        ) {
                            Text("Edit")
                        }

                        if (!category.isBuiltIn) {
                            TextButton(
                                onClick = {
                                    showDeleteConfirmation = category
                                }
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        CategoryEditorDialog(
            category = editingCategory,
            onDismiss = {
                showEditor = false
            },
            onSave = { name, icon, color ->
                val current = editingCategory
                val updated = if (current == null) {
                    ScheduleCategory(
                        name = name,
                        icon = icon,
                        color = color,
                        isBuiltIn = false,
                        sortOrder = 100
                    )
                } else {
                    current.copy(
                        name = name,
                        icon = icon,
                        color = color
                    )
                }

                val callback: (Boolean) -> Unit = { success ->
                    if (success) {
                        showEditor = false
                        errorMessage = null
                    } else {
                        errorMessage =
                            "Couldn't save the category. Check whether its name is already used."
                    }
                }

                if (current == null) {
                    viewModel.createCategory(updated, callback)
                } else {
                    viewModel.updateCategory(updated, callback)
                }
            }
        )
    }

    showDeleteConfirmation?.let { category ->
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = null
            },
            title = {
                Text("Delete ${category.name}?")
            },
            text = {
                Text(
                    "Schedules using this category may prevent deletion. " +
                            "Reassign those schedules to another category first."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategory(category) { success ->
                            if (!success) {
                                errorMessage =
                                    "Couldn't delete this category. " +
                                            "It may still be used by a schedule."
                            } else {
                                errorMessage = null
                            }
                            showDeleteConfirmation = null
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CategoryEditorDialog(
    category: ScheduleCategory?,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, color: Long) -> Unit
) {
    var name by remember(category?.id) {
        mutableStateOf(category?.name.orEmpty())
    }
    var icon by remember(category?.id) {
        mutableStateOf(category?.icon ?: "📌")
    }
    var color by remember(category?.id) {
        mutableStateOf(
            category?.color ?: categoryColors.first().value
        )
    }
    var validationError by remember {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (category == null) "Add category" else "Edit category")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        validationError = null
                    },
                    label = { Text("Category name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Choose an icon",
                    style = MaterialTheme.typography.titleSmall
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoryIcons.chunked(5).forEach { iconRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            iconRow.forEach { option ->
                                if (option == icon) {
                                    Button(
                                        onClick = { icon = option },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(option)
                                    }
                                } else {
                                    TextButton(
                                        onClick = { icon = option },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(option)
                                    }
                                }
                            }
                            repeat(5 - iconRow.size) {
                                androidx.compose.foundation.layout.Spacer(
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Choose a color",
                    style = MaterialTheme.typography.titleSmall
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryColors.chunked(4).forEach { colorRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            colorRow.forEach { option ->
                                val selected = color == option.value
                                OutlinedButton(
                                    onClick = { color = option.value },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 2.dp,
                                        vertical = 6.dp
                                    )
                                ) {
                                    Text(
                                        text = if (selected) {
                                            "✓ ${option.name}"
                                        } else {
                                            option.name
                                        },
                                        color = Color(option.value)
                                    )
                                }
                            }
                            repeat(4 - colorRow.size) {
                                androidx.compose.foundation.layout.Spacer(
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                validationError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cleanName = name.trim()
                    if (cleanName.isBlank()) {
                        validationError = "Enter a category name."
                    } else {
                        onSave(cleanName, icon, color)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

