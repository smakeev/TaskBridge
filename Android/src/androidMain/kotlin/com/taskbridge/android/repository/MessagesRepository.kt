package com.taskbridge.android.repository

import com.taskbridge.core.models.messages.AppMessage
import com.taskbridge.core.models.messages.AppMessageKey
import kotlinx.coroutines.flow.Flow

interface MessagesRepository {
    fun observeAll(): Flow<AppMessage>
    fun observe(types: List<AppMessageKey>): Flow<AppMessage>
    fun observe(type: AppMessageKey): Flow<AppMessage>
    suspend fun publish(message: AppMessage)
}
