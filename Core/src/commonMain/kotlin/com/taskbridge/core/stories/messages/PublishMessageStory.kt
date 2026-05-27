package com.taskbridge.core.stories.messages

import com.taskbridge.core.messages.internal.CoreMessage
import com.taskbridge.core.services.messages.MessagesService

internal class PublishMessageStory(
    private val messagesService: MessagesService
) {
    suspend fun publish(message: CoreMessage) {
        messagesService.publish(message)
    }
}
