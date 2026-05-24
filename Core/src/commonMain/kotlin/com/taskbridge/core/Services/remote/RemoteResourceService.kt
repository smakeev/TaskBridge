package com.taskbridge.core.services.remote

import com.taskbridge.core.services.common.BaseStatefulService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope

/**
 * Generic stateful service for managing remote resource state by URL.
 * Orchestrates loading logic and stores data, errors, and TTL metadata.
 */
internal class RemoteResourceService(
    scope: CoroutineScope,
    private val timeProvider: () -> Long
) : BaseStatefulService<RemoteResourceCommand, RemoteResourceServiceData>(
    initialData = RemoteResourceServiceData(),
    scope = scope
) {

    override suspend fun handleCommand(command: RemoteResourceCommand) {
        val currentEntry = data.value.resource(command.url)
        val now = timeProvider()

        val shouldLoad = when (command) {
            is RemoteResourceCommand.ForceLoad -> {
                // Ignore TTL, but skip if already loading
                currentEntry?.status != RemoteResourceStatus.LOADING
            }
            is RemoteResourceCommand.Load -> {
                when {
                    currentEntry?.status == RemoteResourceStatus.LOADING -> false
                    currentEntry?.status == RemoteResourceStatus.LOADED && currentEntry.loadedAtMillis != null -> {
                        val isExpired = now - currentEntry.loadedAtMillis >= command.ttlMillis
                        if (!isExpired) {
                            // Update metadata even if fresh
                            updateEntryMetadata(command)
                        }
                        isExpired
                    }
                    else -> true
                }
            }
        }

        if (shouldLoad) {
            performLoad(command)
        }
    }

    private suspend fun performLoad(command: RemoteResourceCommand) {
        // 1. Emit LOADING state
        updateState { currentData ->
            val entry = (currentData.resource(command.url) ?: createIdleEntry(command.url)).copy(
                status = RemoteResourceStatus.LOADING,
                errorMessage = null,
                ttlMillis = command.ttlMillis,
                autoRefreshEnabled = command.autoRefreshEnabled
            )
            currentData.copy(resources = currentData.resources + (command.url to entry))
        }

        // 2. Call loader
        try {
            val result = command.loader()
            
            // 3. Emit LOADED state
            updateState { currentData ->
                val entry = (currentData.resource(command.url) ?: createIdleEntry(command.url)).copy(
                    status = RemoteResourceStatus.LOADED,
                    data = result,
                    errorMessage = null,
                    loadedAtMillis = timeProvider(),
                    ttlMillis = command.ttlMillis,
                    autoRefreshEnabled = command.autoRefreshEnabled
                )
                currentData.copy(resources = currentData.resources + (command.url to entry))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            // 4. Emit FAILED state
            updateState { currentData ->
                val entry = (currentData.resource(command.url) ?: createIdleEntry(command.url)).copy(
                    status = RemoteResourceStatus.FAILED,
                    errorMessage = e.message ?: e::class.simpleName ?: "Unknown error",
                    ttlMillis = command.ttlMillis,
                    autoRefreshEnabled = command.autoRefreshEnabled
                )
                currentData.copy(resources = currentData.resources + (command.url to entry))
            }
        }
    }

    private fun updateEntryMetadata(command: RemoteResourceCommand) {
        updateState { currentData ->
            val entry = currentData.resource(command.url)?.copy(
                ttlMillis = command.ttlMillis,
                autoRefreshEnabled = command.autoRefreshEnabled
            )
            if (entry != null) {
                currentData.copy(resources = currentData.resources + (command.url to entry))
            } else {
                currentData
            }
        }
    }

    private fun createIdleEntry(url: String) = RemoteResourceEntry(
        url = url,
        status = RemoteResourceStatus.IDLE,
        data = null,
        errorMessage = null,
        loadedAtMillis = null,
        ttlMillis = 0,
        autoRefreshEnabled = false
    )

    // TODO: Implement background refresh logic using the autoRefreshEnabled flag in the future.
}
