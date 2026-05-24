package com.taskbridge.core.services.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal abstract class BaseStatefulService<C : ServiceCommand, D : ServiceData>(
    initialData: D,
    private val scope: CoroutineScope
) : StatefulService<C, D> {

    private val _data = MutableStateFlow(initialData)
    override val data: StateFlow<D> = _data.asStateFlow()

    private val commandChannel = Channel<C>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (command in commandChannel) {
                handleCommand(command)
            }
        }
    }

    override suspend fun sendCommand(command: C) {
        commandChannel.send(command)
    }

    protected abstract suspend fun handleCommand(command: C)

    protected fun updateState(reducer: (D) -> D) {
        _data.value = reducer(_data.value)
    }
}
