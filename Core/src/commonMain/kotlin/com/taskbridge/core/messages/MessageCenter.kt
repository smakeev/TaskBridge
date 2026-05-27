package com.taskbridge.core.messages

import com.taskbridge.core.messages.internal.CoreMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class MessageCenter {
    private val messages = MutableSharedFlow<CoreMessage>(
        replay = 0,
        extraBufferCapacity = 64
    )

    suspend fun publish(message: CoreMessage) {
        messages.emit(message)
    }

    fun messages(): Flow<CoreMessage> {
        return messages.asSharedFlow()
    }
}
