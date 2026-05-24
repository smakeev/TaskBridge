package com.taskbridge.core.events.reminders

import com.taskbridge.core.models.reminders.Reminder

/**
 * Internal sealed interface for reminder events.
 * These events are emitted by platform handlers to notify Core about reminder changes.
 */
internal sealed interface ReminderEvent {
    /**
     * Event emitted when the full list of reminders has been updated.
     * Used for initial sync, scheduling, and cancellation.
     */
    data class RemindersUpdated(
        val reminders: List<Reminder>
    ) : ReminderEvent
}
