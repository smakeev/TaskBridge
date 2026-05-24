package com.taskbridge.core.stories.remote

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.services.remote.RemoteResourceCommand

/**
 * Internal story for force-loading a remote resource (ignoring TTL).
 * Orchestrates [RemoteResourceService] and [NetworkService].
 */
internal class ForceLoadRemoteResourceStory(
    private val assembler: CoreAssembler
) {
    suspend inline fun <reified T> forceLoad(
        url: String,
        ttlMillis: Long,
        autoRefreshEnabled: Boolean = false
    ) {
        val remoteService = assembler.services.remoteResourceService()
        val networkService = assembler.services.networkService()

        remoteService.sendCommand(
            RemoteResourceCommand.ForceLoad(
                url = url,
                ttlMillis = ttlMillis,
                autoRefreshEnabled = autoRefreshEnabled,
                loader = { networkService.loadJson<T>(url) }
            )
        )
    }
}
