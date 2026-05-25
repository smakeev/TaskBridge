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
 * All state updates for the reminder list flow through the [ReminderEvent.RemindersUpdated] event.
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
        // Subscribe to incoming reminder events from the platform (or internal sync)
        scope.launch {
            reminderEvents.events().collect { event ->
                when (event) {
                    is ReminderEvent.RemindersUpdated -> {
                        println("[TaskBridge][Core][RemindersService] event RemindersUpdated count=${event.reminders.size}")
                        updateState { it.copy(
                            reminders = event.reminders,
                            isLoading = false,
                            errorMessage = null
                        ) }
                    }
                }
            }
        }

        // Perform initial synchronization by emitting an event into the bus
        scope.launch {
            try {
                val initialReminders = reminderHandler.getAllReminders()
                println("[TaskBridge][Core][RemindersService] initial sync count=${initialReminders.size}")
                reminderEvents.emit(ReminderEvent.RemindersUpdated(initialReminders))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                updateState { it.copy(
                    errorMessage = e.message ?: e::class.simpleName ?: "Initial sync error"
                ) }
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
        println("[TaskBridge][Core][RemindersService] load reminders")
        updateState { it.copy(isLoading = true, errorMessage = null) }
        try {
            val reminders = reminderHandler.getAllReminders()
            println("[TaskBridge][Core][RemindersService] load result count=${reminders.size}")
            // Emit update event to maintain single state update path
            reminderEvents.emit(ReminderEvent.RemindersUpdated(reminders))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            updateState { it.copy(
                isLoading = false,
                errorMessage = e.message ?: e::class.simpleName ?: "Load error"
            ) }
        }
    }

    private suspend fun performAction(action: suspend () -> Unit) {
        println("[TaskBridge][Core][RemindersService] perform action")
        updateState { it.copy(isLoading = true, errorMessage = null) }
        try {
            action()
            // We do not update state here manually; we wait for the RemindersUpdated event from the handler.
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            updateState { it.copy(
                isLoading = false,
                errorMessage = e.message ?: e::class.simpleName ?: "Action error"
            ) }
        }
    }
}
