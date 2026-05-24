package com.taskbridge.core.services.common

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for services that maintain a state and process commands.
 */
internal interface StatefulService<C : ServiceCommand, D : ServiceData> : Service {
    val data: StateFlow<D>

    suspend fun sendCommand(command: C)
}
