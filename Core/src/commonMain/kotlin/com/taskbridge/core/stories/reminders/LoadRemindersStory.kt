package com.taskbridge.core.stories.reminders

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.services.reminders.RemindersCommand

/**
 * Internal story for triggering a load of all reminders from the platform handler.
 */
internal class LoadRemindersStory(
    private val assembler: CoreAssembler
) {
    suspend fun loadReminders() {
        assembler.services.remindersService().sendCommand(RemindersCommand.LoadReminders)
    }
}
