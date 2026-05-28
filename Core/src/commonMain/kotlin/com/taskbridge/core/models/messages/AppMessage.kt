package com.taskbridge.core.models.messages

import com.taskbridge.core.models.reminders.ReminderId
import com.taskbridge.core.models.tasks.TaskId

public interface Toastable {
    public val text: String
}

public sealed interface AppMessage {
    public data class ReminderCreated(
        val id: ReminderId,
        override val text: String
    ) : AppMessage, Toastable

    public data class TaskAdded(
        val id: TaskId,
        val parentPath: List<TaskId>,
        override val text: String
    ) : AppMessage, Toastable
}
