package com.taskbridge.core.services.reminders

import com.taskbridge.core.models.reminders.Reminder
import com.taskbridge.core.models.reminders.ReminderId
import com.taskbridge.core.services.common.ServiceCommand

/**
 * Internal commands for the Reminders service.
 */
internal sealed interface RemindersCommand : ServiceCommand {
    /**
     * Triggers a load of all reminders from the platform handler.
     */
    data object LoadReminders : RemindersCommand

    /**
     * Schedules a new reminder.
     */
    data class ScheduleReminder(val reminder: Reminder) : RemindersCommand

    /**
     * Cancels an existing reminder.
     */
    data class CancelReminder(val reminderId: ReminderId) : RemindersCommand
}
