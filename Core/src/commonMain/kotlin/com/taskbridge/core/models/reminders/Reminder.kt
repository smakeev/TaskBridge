package com.taskbridge.core.models.reminders

/**
 * Domain model representing a notification reminder.
 * Reminders are standalone objects that will later be handled by platform-specific logic.
 */
public data class Reminder(
    val id: ReminderId,
    val title: String,
    val body: String,
    val type: ReminderType,
    val triggerAtMillis: Long,
    val createdAtMillis: Long
)
