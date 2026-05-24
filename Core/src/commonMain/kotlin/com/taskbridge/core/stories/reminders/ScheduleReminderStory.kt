package com.taskbridge.core.stories.reminders

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.reminders.Reminder
import com.taskbridge.core.services.reminders.RemindersCommand

/**
 * Internal story for scheduling a new reminder.
 */
internal class ScheduleReminderStory(
    private val assembler: CoreAssembler
) {
    suspend fun scheduleReminder(reminder: Reminder) {
        assembler.services.remindersService().sendCommand(RemindersCommand.ScheduleReminder(reminder))
    }
}
