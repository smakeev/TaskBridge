package com.taskbridge.android.ui.screens.tasks

import androidx.compose.runtime.Composable
import com.taskbridge.core.models.reminders.ReminderType
import com.taskbridge.core.models.tasks.TaskItem

/**
 * The rename + reminder dialogs shared by [TasksRootScreen] and [TaskDetailsScreen].
 * Task-specific (unlike the generic `ScrollBlinkEffects`): each is shown only when its
 * task argument is non-null.
 */
@Composable
fun TaskActionDialogs(
    taskToRename: TaskItem?,
    onRenameDismiss: () -> Unit,
    onRename: (task: TaskItem, newTitle: String) -> Unit,
    taskForReminder: TaskItem?,
    onReminderDismiss: () -> Unit,
    onReminder: (task: TaskItem, title: String, body: String, type: ReminderType, minutesFromNow: Int) -> Unit
) {
    taskToRename?.let { task ->
        TaskRenameDialog(
            task = task,
            onDismiss = onRenameDismiss,
            onSave = { newTitle -> onRename(task, newTitle) }
        )
    }

    taskForReminder?.let { task ->
        TaskReminderDialog(
            task = task,
            onDismiss = onReminderDismiss,
            onSave = { title, body, type, minutesFromNow ->
                onReminder(task, title, body, type, minutesFromNow)
            }
        )
    }
}
