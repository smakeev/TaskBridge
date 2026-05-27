package com.taskbridge.core.stories.messages

import com.taskbridge.core.messages.internal.CoreMessage
import com.taskbridge.core.services.messages.MessagesService
import kotlinx.coroutines.flow.Flow

internal class ObserveMessagesStory(
    private val messagesService: MessagesService
) {
    fun observe(): Flow<CoreMessage> {
        return messagesService.messages()
    }
}
