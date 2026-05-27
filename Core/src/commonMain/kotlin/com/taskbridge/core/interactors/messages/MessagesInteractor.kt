package com.taskbridge.core.interactors.messages

import com.taskbridge.core.messages.internal.CoreMessage
import com.taskbridge.core.models.messages.AppMessage
import com.taskbridge.core.models.messages.AppMessageError
import com.taskbridge.core.models.reminders.ReminderType
import com.taskbridge.core.usecases.messages.MessagesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlin.reflect.KClass

public class MessagesInteractor internal constructor(
    private val messagesUseCase: MessagesUseCase
) {
    private val supportedInboundTypes: Set<KClass<out AppMessage>> = emptySet()

    private data class OutboundMapper(
        val coreType: KClass<out CoreMessage>,
        val map: (Flow<CoreMessage>) -> Flow<AppMessage>
    )

    private val outboundMappers: Map<KClass<out AppMessage>, OutboundMapper> = mapOf(
        AppMessage.ReminderCreated::class to OutboundMapper(
            coreType = CoreMessage.ReminderCreated::class,
            map = { messages ->
                messages
                    .filterIsInstance<CoreMessage.ReminderCreated>()
                    .map { message ->
                        // TODO: Use the upcoming LocalizationHandler to return localized platform-ready text.
                        AppMessage.ReminderCreated(
                            text = "Reminder created: ${message.title} • ${message.type.label()} • ${message.triggerAtMillis.formatAsInstantText()}"
                        )
                    }
            }
        )
    )

    public fun observe(type: KClass<out AppMessage>): Flow<AppMessage> {
        val mapper = outboundMappers[type]
            ?: error("Unsupported app message subscription: ${type.simpleName}")
        val coreMessages = messagesUseCase.observe(mapper.coreType)
        return mapper.map(coreMessages)
    }

    public suspend fun publish(message: AppMessage) {
        val type = message::class
        if (type !in supportedInboundTypes) {
            throw AppMessageError.UnsupportedCoreMessage(typeName = type.simpleName ?: "Unknown")
        }

        // No inbound platform messages are supported yet.
        throw AppMessageError.UnsupportedCoreMessage(typeName = type.simpleName ?: "Unknown")
    }

    private fun ReminderType.label(): String {
        return when (this) {
            ReminderType.START -> "Start"
            ReminderType.DEADLINE -> "Deadline"
        }
    }

    private fun Long.formatAsInstantText(): String {
        return Instant.fromEpochMilliseconds(this).toString()
    }
}
