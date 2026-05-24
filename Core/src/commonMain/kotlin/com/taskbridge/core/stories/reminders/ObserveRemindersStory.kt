package com.taskbridge.core.stories.reminders

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.services.reminders.RemindersServiceData
import kotlinx.coroutines.flow.Flow

/**
 * Internal story for observing the current reminders state.
 */
internal class ObserveRemindersStory(
    private val assembler: CoreAssembler
) {
    fun observeReminders(): Flow<RemindersServiceData> {
        return assembler.services.remindersService().data
    }
}
