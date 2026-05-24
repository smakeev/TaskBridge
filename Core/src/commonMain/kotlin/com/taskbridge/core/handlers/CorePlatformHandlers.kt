package com.taskbridge.core.handlers

import com.taskbridge.core.handlers.reminders.ReminderHandler

/**
 * Container for all platform-provided capability handlers.
 */
public data class CorePlatformHandlers(
    val reminderHandler: ReminderHandler
)
