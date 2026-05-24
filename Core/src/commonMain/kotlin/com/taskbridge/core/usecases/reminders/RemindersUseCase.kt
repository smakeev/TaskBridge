package com.taskbridge.core.usecases.reminders

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.reminders.Reminder
import com.taskbridge.core.models.reminders.ReminderId
import com.taskbridge.core.services.reminders.RemindersServiceData
import kotlinx.coroutines.flow.Flow

/**
 * Internal use case for managing reminders.
 * Orchestrates reminder stories to provide domain-level operations.
 */
public class RemindersUseCase internal constructor(
    private val assembler: CoreAssembler
) {
    public suspend fun loadReminders() {
        assembler.stories.loadReminders(assembler).loadReminders()
    }

    public suspend fun scheduleReminder(reminder: Reminder) {
        assembler.stories.scheduleReminder(assembler).scheduleReminder(reminder)
    }

    public suspend fun cancelReminder(reminderId: ReminderId) {
        assembler.stories.cancelReminder(assembler).cancelReminder(reminderId)
    }

    /**
     * Provides access to the raw internal reminder service data flow.
     */
    internal fun observeReminders(): Flow<RemindersServiceData> {
        return assembler.stories.observeReminders(assembler).observeReminders()
    }
}
