package com.taskbridge.android.repository.impl

import com.taskbridge.android.repository.RemindersRepository
import com.taskbridge.core.interactors.reminders.RemindersInteractor
import com.taskbridge.core.models.reminders.Reminder
import com.taskbridge.core.models.reminders.ReminderId
import com.taskbridge.core.usecases.reminders.RemindersState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Implementation of [RemindersRepository] that delegates to [RemindersInteractor].
 */
class RemindersRepositoryImpl(
    private val interactor: RemindersInteractor,
    scope: CoroutineScope
) : RemindersRepository {

    override val remindersState: StateFlow<RemindersState> = interactor.remindersState
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RemindersState()
        )

    override suspend fun scheduleReminder(reminder: Reminder) {
        interactor.scheduleReminder(reminder)
    }

    override suspend fun cancelReminder(reminderId: ReminderId) {
        interactor.cancelReminder(reminderId)
    }
}
