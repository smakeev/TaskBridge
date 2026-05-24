package com.taskbridge.core.services.network

import com.taskbridge.core.services.common.ServiceRequest

/**
 * Internal sealed interface for network requests.
 */
internal sealed interface NetworkRequest : ServiceRequest {
    /**
     * Request type for executing a JSON load.
     * @param url The target URL.
     * @param loader A suspension-based loader that returns the deserialized object.
     */
    data class ExecuteJson(
        val url: String,
        val loader: suspend () -> Any?
    ) : NetworkRequest
}
