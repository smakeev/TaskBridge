package com.taskbridge.core.services.messages

import com.taskbridge.core.messages.MessageCenter
import com.taskbridge.core.messages.internal.CoreMessage
import com.taskbridge.core.services.common.BaseStatelessService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

internal class MessagesService(
    private val messageCenter: MessageCenter,
    scope: CoroutineScope
) : BaseStatelessService<MessagesRequest, Unit>(scope) {
    suspend fun publish(message: CoreMessage) {
        execute(MessagesRequest.Publish(message))
    }

    fun messages(): Flow<CoreMessage> {
        return messageCenter.messages()
    }

    override suspend fun handleRequest(request: MessagesRequest) {
        when (request) {
            is MessagesRequest.Publish -> messageCenter.publish(request.message)
        }
    }
}
