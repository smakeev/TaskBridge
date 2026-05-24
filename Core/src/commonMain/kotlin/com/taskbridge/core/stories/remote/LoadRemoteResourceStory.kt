package com.taskbridge.core.stories.remote

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.services.remote.RemoteResourceCommand

/**
 * Internal story for loading a remote resource with TTL respect.
 * Orchestrates [RemoteResourceService] and [NetworkService].
 */
internal class LoadRemoteResourceStory(
    private val assembler: CoreAssembler
) {
    suspend inline fun <reified T> load(
        url: String,
        ttlMillis: Long,
        autoRefreshEnabled: Boolean = false
    ) {
        val remoteService = assembler.services.remoteResourceService()
        val networkService = assembler.services.networkService()

        remoteService.sendCommand(
            RemoteResourceCommand.Load(
                url = url,
                ttlMillis = ttlMillis,
                autoRefreshEnabled = autoRefreshEnabled,
                loader = { networkService.loadJson<T>(url) }
            )
        )
    }
}
