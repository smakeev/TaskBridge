package com.taskbridge.android.ui.screens.templates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taskbridge.core.models.templates.TemplateTaskItem
import com.taskbridge.core.models.templates.TemplateTaskType

@Composable
fun TemplateTreeView(
    rootTask: TemplateTaskItem,
    expandedNodeIds: Set<String>,
    onToggle: (String) -> Unit
) {
    Column {
        TemplateTaskRow(
            task = rootTask,
            depth = 0,
            expandedNodeIds = expandedNodeIds,
            onToggle = onToggle
        )
    }
}

@Composable
private fun TemplateTaskRow(
    task: TemplateTaskItem,
    depth: Int,
    expandedNodeIds: Set<String>,
    onToggle: (String) -> Unit
) {
    val children = task.children.orEmpty()
    val isContainer = task.type == TemplateTaskType.CONTAINER
    val isExpanded = expandedNodeIds.contains(task.id)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isContainer) {
                        Modifier.clickable { onToggle(task.id) }
                    } else {
                        Modifier
                    }
                )
                .padding(start = (minOf(depth, 4) * 16).dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isContainer) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(18.dp))
            }

            Icon(
                imageVector = when (task.type) {
                    TemplateTaskType.CHECKBOX -> Icons.Default.CheckCircle
                    TemplateTaskType.PROGRESS -> Icons.AutoMirrored.Filled.List
                    TemplateTaskType.CONTAINER -> Icons.AutoMirrored.Filled.List
                },
                contentDescription = null,
                tint = taskIconColor(task.type),
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .size(20.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (depth == 0) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.type == TemplateTaskType.PROGRESS && task.initialProgress != null) {
                    Text(
                        text = "${task.initialProgress}% initial progress",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (isContainer && isExpanded) {
            children.forEach { child ->
                TemplateTaskRow(
                    task = child,
                    depth = depth + 1,
                    expandedNodeIds = expandedNodeIds,
                    onToggle = onToggle
                )
            }
        }
    }
}

@Composable
private fun taskIconColor(type: TemplateTaskType): Color {
    return when (type) {
        TemplateTaskType.CHECKBOX -> Color(0xFF2E7D32)
        TemplateTaskType.PROGRESS -> MaterialTheme.colorScheme.primary
        TemplateTaskType.CONTAINER -> Color(0xFFE17800)
    }
}
