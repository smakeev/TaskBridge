package com.taskbridge.core.events

import com.taskbridge.core.events.reminders.ReminderEvent
import com.taskbridge.core.models.reminders.Reminder

/**
 * Public facade for emitting events from the platform layer into the Core.
 */
public class CoreEventEmitter internal constructor(
    private val buses: CoreEventBuses
) {
    /**
     * Notifies Core that the list of reminders has been updated on the platform.
     */
    public suspend fun remindersUpdated(reminders: List<Reminder>) {
        buses.reminderEvents.emit(ReminderEvent.RemindersUpdated(reminders))
    }
}
