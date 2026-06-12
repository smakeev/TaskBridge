package com.taskbridge.core.stories.messages

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.messages.internal.CoreMessage

internal class PublishMessageStory(
    private val assembler: CoreAssembler
) {
    suspend fun publish(message: CoreMessage) {
        assembler.services.messagesService().publish(message)
    }
}
