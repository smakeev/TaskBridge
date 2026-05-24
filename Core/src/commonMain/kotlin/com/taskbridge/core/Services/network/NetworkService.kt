package com.taskbridge.core.services.network

import com.taskbridge.core.network.JsonRequestManager
import com.taskbridge.core.services.common.BaseStatelessService
import kotlinx.coroutines.CoroutineScope

/**
 * Internal stateless service for network operations.
 * Wraps [JsonRequestManager] and provides sequential request handling.
 * Does not cache completed results.
 */
internal class NetworkService(
    private val requestManager: JsonRequestManager,
    scope: CoroutineScope
) : BaseStatelessService<NetworkRequest, Any?>(scope) {

    /**
     * Loads JSON from the specified [url] and deserializes it as [T].
     * Utilizes [JsonRequestManager] for in-flight deduplication.
     */
    suspend inline fun <reified T> loadJson(url: String): T {
        val request = NetworkRequest.ExecuteJson(
            url = url,
            loader = { requestManager.loadJson<T>(url) }
        )
        return execute(request) as T
    }

    override suspend fun handleRequest(request: NetworkRequest): Any? {
        return when (request) {
            is NetworkRequest.ExecuteJson -> request.loader()
        }
    }
}
