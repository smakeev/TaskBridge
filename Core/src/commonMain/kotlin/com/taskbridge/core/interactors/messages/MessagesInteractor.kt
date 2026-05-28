package com.taskbridge.core.interactors.messages

import com.taskbridge.core.messages.internal.CoreMessage
import com.taskbridge.core.models.messages.AppMessage
import com.taskbridge.core.models.messages.AppMessageError
import com.taskbridge.core.models.messages.AppMessageKey
import com.taskbridge.core.models.reminders.ReminderType
import com.taskbridge.core.usecases.messages.MessagesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.Instant
import kotlin.reflect.KClass

public class MessagesInteractor internal constructor(
    private val messagesUseCase: MessagesUseCase
) {
    private val supportedInboundTypes: Set<KClass<out AppMessage>> = emptySet()

    private data class OutboundMapper(
        val coreType: KClass<out CoreMessage>,
        val mapMessage: (CoreMessage) -> AppMessage?
    )

    private val outboundMappers: Map<KClass<out AppMessage>, OutboundMapper> = mapOf(
        AppMessage.ReminderCreated::class to OutboundMapper(
            coreType = CoreMessage.ReminderCreated::class,
            mapMessage = { message ->
                (message as? CoreMessage.ReminderCreated)?.toAppMessage()
            },
        )
    )

    public fun observeAll(): Flow<AppMessage> {
        return messagesUseCase.observeAll()
            .mapNotNull { message -> mapCoreMessage(message) }
    }

    public fun observe(type: AppMessageKey): Flow<AppMessage> {
        val mapper = mapperFor(type)
        return messagesUseCase.observe(mapper.coreType)
            .mapNotNull { message -> mapper.mapMessage(message) }
    }

    public fun observe(types: List<AppMessageKey>): Flow<AppMessage> {
        val mappers = types.toSet().map { type -> mapperFor(type) }
        return messagesUseCase.observe(mappers.map { mapper -> mapper.coreType })
            .mapNotNull { message -> mapCoreMessage(message) }
    }

    public suspend fun publish(message: AppMessage) {
        val type = message::class
        if (type !in supportedInboundTypes) {
            throw AppMessageError.UnsupportedCoreMessage(typeName = type.simpleName ?: "Unknown")
        }

        // No inbound platform messages are supported yet.
        throw AppMessageError.UnsupportedCoreMessage(typeName = type.simpleName ?: "Unknown")
    }

    private fun mapCoreMessage(message: CoreMessage): AppMessage? {
        return outboundMappers.values
            .firstOrNull { mapper -> mapper.coreType.isInstance(message) }
            ?.mapMessage(message)
    }

    private fun mapperFor(type: AppMessageKey): OutboundMapper {
        return outboundMappers[type.type]
            ?: error("Unsupported app message subscription: ${type.type.simpleName}")
    }

    private fun CoreMessage.ReminderCreated.toAppMessage(): AppMessage.ReminderCreated {
        // TODO: Use the upcoming LocalizationHandler to return localized platform-ready text.
        return AppMessage.ReminderCreated(
            text = "Reminder created: $title • ${type.label()} • ${triggerAtMillis.formatAsInstantText()}"
        )
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
