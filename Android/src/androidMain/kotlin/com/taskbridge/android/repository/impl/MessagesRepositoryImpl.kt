package com.taskbridge.android.repository.impl

import com.taskbridge.android.repository.MessagesRepository
import com.taskbridge.core.interactors.messages.MessagesInteractor
import com.taskbridge.core.models.messages.AppMessage
import com.taskbridge.core.models.messages.AppMessageKey
import kotlinx.coroutines.flow.Flow

class MessagesRepositoryImpl(
    private val interactor: MessagesInteractor
) : MessagesRepository {
    override fun observeAll(): Flow<AppMessage> {
        return interactor.observeAll()
    }

    override fun observe(types: List<AppMessageKey>): Flow<AppMessage> {
        return interactor.observe(types)
    }

    override fun observe(type: AppMessageKey): Flow<AppMessage> {
        return interactor.observe(type)
    }

    override suspend fun publish(message: AppMessage) {
        interactor.publish(message = message)
    }
}
