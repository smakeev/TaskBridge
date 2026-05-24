package com.taskbridge.core.handlers.reminders

import com.taskbridge.core.events.CoreEventEmitter
import com.taskbridge.core.models.reminders.Reminder
import com.taskbridge.core.models.reminders.ReminderId

/**
 * Public interface for platform-specific reminder handling.
 * Implementations are responsible for real notification scheduling.
 */
public interface ReminderHandler {
    /**
     * Injects the Core event emitter into the handler.
     */
    fun setEventEmitter(emitter: CoreEventEmitter)

    /**
     * Retrieves all currently scheduled reminders.
     */
    suspend fun getAllReminders(): List<Reminder>

    /**
     * Schedules a new reminder/notification.
     */
    suspend fun scheduleReminder(reminder: Reminder)

    /**
     * Cancels an existing reminder/notification.
     */
    suspend fun cancelReminder(reminderId: ReminderId)
}
