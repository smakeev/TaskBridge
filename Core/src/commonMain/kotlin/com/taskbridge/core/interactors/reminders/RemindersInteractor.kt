package com.taskbridge.core.interactors.reminders

import com.taskbridge.core.models.reminders.Reminder
import com.taskbridge.core.models.reminders.ReminderId
import com.taskbridge.core.usecases.reminders.RemindersState
import com.taskbridge.core.usecases.reminders.RemindersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Platform-facing interactor for managing reminders.
 * Maps internal service data to public [RemindersState].
 */
public class RemindersInteractor internal constructor(
    private val useCase: RemindersUseCase
) {
    /**
     * Observable flow of the current reminders state.
     */
    public val remindersState: Flow<RemindersState> = useCase.observeReminders()
        .map { data ->
            RemindersState(
                reminders = data.reminders,
                isLoading = data.isLoading,
                errorMessage = data.errorMessage
            )
        }
        .distinctUntilChanged()

    public suspend fun loadReminders() {
        useCase.loadReminders()
    }

    public suspend fun scheduleReminder(reminder: Reminder) {
        useCase.scheduleReminder(reminder)
    }

    public suspend fun cancelReminder(reminderId: ReminderId) {
        useCase.cancelReminder(reminderId)
    }
}
