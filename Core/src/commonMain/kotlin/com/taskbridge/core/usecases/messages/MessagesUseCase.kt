package com.taskbridge.core.usecases.messages

import com.taskbridge.core.messages.internal.CoreMessage
import com.taskbridge.core.stories.messages.ObserveMessagesStory
import com.taskbridge.core.stories.messages.PublishMessageStory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlin.reflect.KClass

internal class MessagesUseCase(
    private val publishMessageStory: PublishMessageStory,
    private val observeMessagesStory: ObserveMessagesStory
) {
    fun observeAll(): Flow<CoreMessage> {
        return observeMessagesStory.observe()
    }

    fun observe(type: KClass<out CoreMessage>): Flow<CoreMessage> {
        return observe(types = listOf(type))
    }

    fun observe(types: List<KClass<out CoreMessage>>): Flow<CoreMessage> {
        val uniqueTypes = types.toSet()
        if (uniqueTypes.isEmpty()) {
            return emptyFlow()
        }
        return observeAll().filter { message ->
            uniqueTypes.any { type -> type.isInstance(message) }
        }
    }

    suspend fun publish(message: CoreMessage) {
        publishMessageStory.publish(message)
    }
}
