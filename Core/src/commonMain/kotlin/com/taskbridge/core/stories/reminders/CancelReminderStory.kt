package com.taskbridge.core.stories.reminders

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.reminders.ReminderId
import com.taskbridge.core.services.reminders.RemindersCommand

/**
 * Internal story for canceling an existing reminder.
 */
internal class CancelReminderStory(
    private val assembler: CoreAssembler
) {
    suspend fun cancelReminder(reminderId: ReminderId) {
        assembler.services.remindersService().sendCommand(RemindersCommand.CancelReminder(reminderId))
    }
}
