package com.taskbridge.core.interactors.reminders

import com.taskbridge.core.composition.CoreAccess
import com.taskbridge.core.models.reminders.Reminder
import com.taskbridge.core.models.reminders.ReminderId
import com.taskbridge.core.usecases.reminders.RemindersState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Platform-facing interactor for managing reminders.
 * Maps internal service data to public [RemindersState].
 *
 * The [accessToken] parameter prevents construction outside the Core module:
 * only Core can mint a [CoreAccess] instance. The internal use case is
 * pulled from the token; its type is not exposed in this signature.
 */
public class RemindersInteractor public constructor(
    accessToken: CoreAccess
) {
    private val useCase = accessToken.dependencies.remindersUseCase
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
