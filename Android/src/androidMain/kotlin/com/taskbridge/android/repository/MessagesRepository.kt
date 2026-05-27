package com.taskbridge.android.repository

import com.taskbridge.core.models.messages.AppMessage
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

interface MessagesRepository {
    fun observe(type: KClass<out AppMessage>): Flow<AppMessage>
    suspend fun publish(message: AppMessage)
}
