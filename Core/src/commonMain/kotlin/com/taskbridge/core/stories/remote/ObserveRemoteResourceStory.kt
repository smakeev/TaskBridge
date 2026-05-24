package com.taskbridge.core.stories.remote

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.services.remote.RemoteResourceEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Internal story for observing the state of a remote resource by URL.
 */
internal class ObserveRemoteResourceStory(
    private val assembler: CoreAssembler
) {
    fun observe(url: String): Flow<RemoteResourceEntry?> {
        val remoteService = assembler.services.remoteResourceService()
        return remoteService.data
            .map { it.resource(url) }
            .distinctUntilChanged()
    }
}
