package com.taskbridge.core.services.reminders

import com.taskbridge.core.events.common.CoreEventBus
import com.taskbridge.core.events.reminders.ReminderEvent
import com.taskbridge.core.handlers.reminders.ReminderHandler
import com.taskbridge.core.services.common.BaseStatefulService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Stateful service for managing reminders.
 * Synchronizes Core state with platform-specific [ReminderHandler] via event-driven updates.
 */
internal class RemindersService(
    private val scope: CoroutineScope,
    private val reminderHandler: ReminderHandler,
    private val reminderEvents: CoreEventBus<ReminderEvent>
) : BaseStatefulService<RemindersCommand, RemindersServiceData>(
    initialData = RemindersServiceData(),
    scope = scope
) {

    init {
        // Subscribe to incoming reminder events from the platform
        scope.launch {
            reminderEvents.events().collect { event ->
                when (event) {
                    is ReminderEvent.RemindersUpdated -> {
                        updateState { it.copy(
                            reminders = event.reminders,
                            isLoading = false,
                            errorMessage = null
                        ) }
                    }
                }
            }
        }

        // Perform initial synchronization
        scope.launch {
            try {
                val initialReminders = reminderHandler.getAllReminders()
                updateState { it.copy(reminders = initialReminders) }
            } catch (e: Exception) {
                // Initial sync failure is not fatal but should be logged in production
            }
        }
    }

    override suspend fun handleCommand(command: RemindersCommand) {
        when (command) {
            is RemindersCommand.LoadReminders -> performLoad()
            is RemindersCommand.ScheduleReminder -> performAction {
                reminderHandler.scheduleReminder(command.reminder)
            }
            is RemindersCommand.CancelReminder -> performAction {
                reminderHandler.cancelReminder(command.reminderId)
            }
        }
    }

    private suspend fun performLoad() {
        updateState { it.copy(isLoading = true, errorMessage = null) }
        try {
            val reminders = reminderHandler.getAllReminders()
            updateState { it.copy(reminders = reminders, isLoading = false, errorMessage = null) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            updateState { it.copy(
                isLoading = false,
                errorMessage = e.message ?: e::class.simpleName ?: "Unknown error"
            ) }
        }
    }

    private suspend fun performAction(action: suspend () -> Unit) {
        updateState { it.copy(isLoading = true, errorMessage = null) }
        try {
            action()
            // We do not update state here manually; we wait for the RemindersUpdated event from the handler.
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            updateState { it.copy(
                isLoading = false,
                errorMessage = e.message ?: e::class.simpleName ?: "Unknown error"
            ) }
        }
    }
}
