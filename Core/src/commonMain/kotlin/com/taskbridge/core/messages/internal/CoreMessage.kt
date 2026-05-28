package com.taskbridge.core.messages.internal

import com.taskbridge.core.models.reminders.ReminderId
import com.taskbridge.core.models.reminders.ReminderType
import com.taskbridge.core.models.tasks.TaskId
import com.taskbridge.core.models.tasks.TaskItem

internal sealed interface CoreMessage {
    data class ReminderCreated(
        val reminderId: ReminderId,
        val title: String,
        val type: ReminderType,
        val triggerAtMillis: Long
    ) : CoreMessage

    data class TaskAdded(
        val task: TaskItem,
        val parentPath: List<TaskId>
    ) : CoreMessage
}
