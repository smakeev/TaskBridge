package com.taskbridge.android.ui.screens.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taskbridge.core.models.tasks.TaskItem
import com.taskbridge.core.models.tasks.TaskType

@Composable
fun TaskTreeRows(
    task: TaskItem,
    depth: Int,
    onOpenTask: (TaskItem) -> Unit,
    onToggleCheckbox: (TaskItem) -> Unit,
    onProgressChanged: (TaskItem, Int) -> Unit,
    onRename: (TaskItem) -> Unit,
    onDelete: (TaskItem) -> Unit
) {
    TaskRow(
        task = task,
        depth = depth,
        onOpenTask = onOpenTask,
        onToggleCheckbox = onToggleCheckbox,
        onProgressChanged = onProgressChanged,
        onRename = onRename,
        onDelete = onDelete
    )
}

@Composable
fun TaskRow(
    task: TaskItem,
    depth: Int,
    onOpenTask: (TaskItem) -> Unit,
    onToggleCheckbox: (TaskItem) -> Unit,
    onProgressChanged: (TaskItem, Int) -> Unit,
    onRename: (TaskItem) -> Unit,
    onDelete: (TaskItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (minOf(depth, 4) * 14).dp)
            .clickable { onOpenTask(task) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (depth == 0) 1.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = taskIcon(task),
                contentDescription = null,
                tint = taskColor(task),
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (depth == 0) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = taskStatus(task),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (task.type == TaskType.PROGRESS) {
                    Slider(
                        value = (task.progress?.value ?: 0).toFloat(),
                        onValueChange = { onProgressChanged(task, it.toInt()) },
                        valueRange = 0f..100f,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            if (task.type == TaskType.CHECKBOX) {
                IconButton(onClick = { onToggleCheckbox(task) }) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Toggle",
                        tint = if (task.isDone == true) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                    )
                }
            }
            IconButton(onClick = { onRename(task) }) {
                Icon(Icons.Default.Edit, contentDescription = "Rename")
            }
            IconButton(onClick = { onDelete(task) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
private fun taskIcon(task: TaskItem) = when (task.type) {
    TaskType.CHECKBOX -> Icons.Default.CheckCircle
    TaskType.PROGRESS -> Icons.AutoMirrored.Filled.List
    TaskType.CONTAINER -> Icons.AutoMirrored.Filled.List
}

@Composable
private fun taskColor(task: TaskItem): Color = when (task.type) {
    TaskType.CHECKBOX -> if (task.isDone == true) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
    TaskType.PROGRESS -> MaterialTheme.colorScheme.primary
    TaskType.CONTAINER -> Color(0xFFE17800)
}

fun taskStatus(task: TaskItem): String = when (task.type) {
    TaskType.CHECKBOX -> if (task.isDone == true) "Checked" else "Unchecked"
    TaskType.PROGRESS -> "${task.progress?.value ?: 0}% complete"
    TaskType.CONTAINER -> subtaskCountText(task.children.size)
}

private fun subtaskCountText(count: Int): String {
    return if (count == 1) "1 subtask" else "$count subtasks"
}
