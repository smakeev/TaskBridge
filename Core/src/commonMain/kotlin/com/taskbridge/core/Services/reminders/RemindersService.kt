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
 * Initial state is seeded directly from the handler; subsequent updates flow through the
 * [ReminderEvent.RemindersUpdated] event.
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
    init {
        // Seed initial state directly, then subscribe to incoming reminder events from the platform
        scope.launch {
            runInitialSync()
            reminderEvents.events().collect { event ->
                when (event) {
                    is ReminderEvent.RemindersUpdated -> {
                        println("[TaskBridge][Core][RemindersService] event RemindersUpdated count=${event.reminders.size}")
                        publishNewReminderMessages(
                            previousReminders = data.value.reminders,
                            updatedReminders = event.reminders
                        )
                        handleRemindersUpdate(event.reminders)
                    }
                }
            }
        }
    }

    private suspend fun runInitialSync() {
        try {
            val initialReminders = reminderHandler.getAllReminders()
            println("[TaskBridge][Core][RemindersService] initial sync count=${initialReminders.size}")
            handleRemindersUpdate(initialReminders)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            updateState { it.copy(
                errorMessage = e.message ?: e::class.simpleName ?: "Initial sync error"
            ) }
        }
    }

    private suspend fun handleRemindersUpdate(reminders: List<Reminder>) {
        updateState { it.copy(
            reminders = reminders,
            isLoading = false,
            errorMessage = null
        ) }
    }

    override suspend fun handleCommand(command: RemindersCommand) {
        when (command) {
            is RemindersCommand.LoadReminders -> performAction {
                val reminders = reminderHandler.getAllReminders()
                reminderEvents.emit(ReminderEvent.RemindersUpdated(reminders))
            }
            is RemindersCommand.ScheduleReminder -> performAction {
                reminderHandler.scheduleReminder(command.reminder)
            }
            is RemindersCommand.CancelReminder -> performAction {
                reminderHandler.cancelReminder(command.reminderId)
            }
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
