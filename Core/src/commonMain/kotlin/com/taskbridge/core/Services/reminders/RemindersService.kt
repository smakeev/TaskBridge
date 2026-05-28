package com.taskbridge.core.services.reminders

import com.taskbridge.core.events.common.CoreEventBus
import com.taskbridge.core.events.reminders.ReminderEvent
import com.taskbridge.core.handlers.reminders.ReminderHandler
import com.taskbridge.core.messages.internal.CoreMessage
import com.taskbridge.core.models.reminders.Reminder
import com.taskbridge.core.services.common.BaseStatefulService
import com.taskbridge.core.stories.messages.PublishMessageStory
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
    private val reminderEvents: CoreEventBus<ReminderEvent>,
    private val publishMessageStory: PublishMessageStory
) : BaseStatefulService<RemindersCommand, RemindersServiceData>(
    initialData = RemindersServiceData(),
    scope = scope
) {
    private var hasCompletedInitialSync = false

    init {
        // Subscribe to incoming reminder events from the platform (or internal sync)
        scope.launch {
            reminderEvents.events().collect { event ->
                when (event) {
                    is ReminderEvent.RemindersUpdated -> {
                        println("[TaskBridge][Core][RemindersService] event RemindersUpdated count=${event.reminders.size}")
                        if (hasCompletedInitialSync) {
                            publishNewReminderMessages(
                                previousReminders = data.value.reminders,
                                updatedReminders = event.reminders
                            )
                        } else {
                            hasCompletedInitialSync = true
                        }
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
            is RemindersCommand.ScheduleReminder -> performSchedule(command)
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

    private suspend fun performSchedule(command: RemindersCommand.ScheduleReminder) {
        println("[TaskBridge][Core][RemindersService] perform schedule")
        updateState { it.copy(isLoading = true, errorMessage = null) }
        try {
            reminderHandler.scheduleReminder(command.reminder)
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

    private suspend fun publishNewReminderMessages(
        previousReminders: List<Reminder>,
        updatedReminders: List<Reminder>
    ) {
        val previousIds = previousReminders.map { reminder -> reminder.id }.toSet()
        updatedReminders
            .filter { reminder -> reminder.id !in previousIds }
            .forEach { reminder ->
                publishMessageStory.publish(
                    CoreMessage.ReminderCreated(
                        reminderId = reminder.id,
                        title = reminder.title,
                        type = reminder.type,
                        triggerAtMillis = reminder.triggerAtMillis
                    )
                )
            }
    }
}
