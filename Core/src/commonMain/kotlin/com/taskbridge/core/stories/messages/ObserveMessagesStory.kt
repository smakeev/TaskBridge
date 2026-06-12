package com.taskbridge.core.stories.messages

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.messages.internal.CoreMessage
import kotlinx.coroutines.flow.Flow

internal class ObserveMessagesStory(
    private val assembler: CoreAssembler
) {
    fun observe(): Flow<CoreMessage> {
        return assembler.services.messagesService().messages()
    }
}
