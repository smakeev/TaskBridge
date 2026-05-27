package com.taskbridge.core.models.messages

public sealed interface AppMessage {
    public data class ReminderCreated(
        val text: String
    ) : AppMessage
}
