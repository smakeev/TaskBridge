package com.taskbridge.core.messages.internal

import com.taskbridge.core.models.reminders.ReminderId
import com.taskbridge.core.models.reminders.ReminderType

internal sealed interface CoreMessage {
    data class ReminderCreated(
        val reminderId: ReminderId,
        val title: String,
        val type: ReminderType,
        val triggerAtMillis: Long
    ) : CoreMessage
}
