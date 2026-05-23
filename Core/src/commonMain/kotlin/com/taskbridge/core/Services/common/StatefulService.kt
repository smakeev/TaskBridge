package com.taskbridge.core.services.common

import kotlinx.coroutines.flow.StateFlow

interface StatefulService<C : ServiceCommand, D : ServiceData> : Service {
    val data: StateFlow<D>

    suspend fun sendCommand(command: C)
}
