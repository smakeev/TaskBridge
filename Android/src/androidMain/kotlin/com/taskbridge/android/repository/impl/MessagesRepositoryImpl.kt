package com.taskbridge.android.repository.impl

import com.taskbridge.android.repository.MessagesRepository
import com.taskbridge.core.interactors.messages.MessagesInteractor
import com.taskbridge.core.models.messages.AppMessage
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

class MessagesRepositoryImpl(
    private val interactor: MessagesInteractor
) : MessagesRepository {
    override fun observe(type: KClass<out AppMessage>): Flow<AppMessage> {
        return interactor.observe(type)
    }

    override suspend fun publish(message: AppMessage) {
        interactor.publish(message = message)
    }
}
