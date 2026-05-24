package com.taskbridge.core.models.reminders

/**
 * Enum representing the type of reminder.
 */
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
