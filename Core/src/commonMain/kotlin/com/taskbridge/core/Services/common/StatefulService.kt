package com.taskbridge.core.services.common

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for services that maintain a state and process commands.
 */
public interface StatefulService<C : ServiceCommand, D : ServiceData> : Service {
    public val data: StateFlow<D>

    public suspend fun sendCommand(command: C)
}
