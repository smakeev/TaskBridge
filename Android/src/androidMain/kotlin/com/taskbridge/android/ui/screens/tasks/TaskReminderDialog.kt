package com.taskbridge.android.ui.screens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.taskbridge.core.models.reminders.ReminderType
import com.taskbridge.core.models.tasks.TaskItem

@Composable
fun TaskReminderDialog(
    task: TaskItem,
    onDismiss: () -> Unit,
    onSave: (title: String, body: String, type: ReminderType, minutesFromNow: Int) -> Unit
) {
    var title by remember(task.id) { mutableStateOf(task.title) }
    var body by remember(task.id) { mutableStateOf("") }
    var type by remember(task.id) { mutableStateOf(ReminderType.START) }
    var minutesText by remember(task.id) { mutableStateOf("60") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    label = { Text("Title") }
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    minLines = 2,
                    label = { Text("Body") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == ReminderType.START,
                        onClick = { type = ReminderType.START },
                        label = { Text("Start") }
                    )
                    FilterChip(
                        selected = type == ReminderType.DEADLINE,
                        onClick = { type = ReminderType.DEADLINE },
                        label = { Text("Deadline") }
                    )
                }
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { value -> minutesText = value.filter { it.isDigit() }.take(5) },
                    singleLine = true,
                    label = { Text("Remind in minutes") },
                    supportingText = { Text("Use Tasks to add more reminders later.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = { onSave(title, body, type, minutesText.toIntOrNull() ?: 60) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
