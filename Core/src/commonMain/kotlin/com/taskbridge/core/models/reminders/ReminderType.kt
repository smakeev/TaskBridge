package com.taskbridge.core.models.reminders

import kotlinx.serialization.Serializable

/**
 * Enum representing the type of reminder.
 */
@Serializable
public enum class ReminderType {
    /**
     * Reminder at an exact time.
     */
    START,
    
    /**
     * Reminder before a specific deadline.
     */
    DEADLINE
}
