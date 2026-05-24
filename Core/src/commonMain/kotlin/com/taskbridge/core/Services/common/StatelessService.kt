package com.taskbridge.core.services.common

/**
 * Generic interface for services that process requests and return results.
 * @param R The type of request.
 * @param T The type of result.
 */
internal interface StatelessService<R : ServiceRequest, T> : Service {
    /**
     * Executes the given [request] and returns the result.
     */
    suspend fun execute(request: R): T
}
