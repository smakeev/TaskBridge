package com.taskbridge.core.events.common

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Internal generic thread-safe event bus.
 * Uses SharedFlow to broadcast events to multiple internal subscribers.
 */
internal class CoreEventBus<E> {
    private val _events = MutableSharedFlow<E>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    /**
     * Emits a new event to all subscribers.
     */
    suspend fun emit(event: E) {
        _events.emit(event)
    }

    /**
     * Provides a flow of events for internal subscription.
     */
    fun events(): Flow<E> = _events.asSharedFlow()
}
