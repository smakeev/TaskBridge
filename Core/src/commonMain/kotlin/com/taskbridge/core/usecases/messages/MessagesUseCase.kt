package com.taskbridge.core.usecases.messages

import com.taskbridge.core.messages.internal.CoreMessage
import com.taskbridge.core.stories.messages.ObserveMessagesStory
import com.taskbridge.core.stories.messages.PublishMessageStory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlin.reflect.KClass

internal class MessagesUseCase(
    private val publishMessageStory: PublishMessageStory,
    private val observeMessagesStory: ObserveMessagesStory
) {
    fun observe(type: KClass<out CoreMessage>): Flow<CoreMessage> {
        return observeMessagesStory.observe().filter { message -> type.isInstance(message) }
    }

    suspend fun publish(message: CoreMessage) {
        publishMessageStory.publish(message)
    }
}
