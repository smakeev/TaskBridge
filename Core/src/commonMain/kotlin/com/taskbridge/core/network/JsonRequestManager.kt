package com.taskbridge.core.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Internal manager for deduplicating in-flight JSON requests.
 * Ensures that concurrent requests to the same URL share the same execution.
 * Does not cache completed results.
 */
internal class JsonRequestManager(
    private val client: HttpJsonClient,
    private val scope: CoroutineScope
) {
    private val mutex = Mutex()
    private val inFlightRequests = mutableMapOf<String, Deferred<Any?>>()

    /**
     * Loads JSON from the specified [url] and deserializes it as [T].
     * Deduplicates in-flight requests by URL.
     */
    suspend inline fun <reified T> loadJson(url: String): T {
        val deferred = mutex.withLock {
            inFlightRequests.getOrPut(url) {
                scope.async {
                    try {
                        client.getJson<T>(url)
                    } finally {
                        // Remove from map regardless of success, failure, or cancellation
                        mutex.withLock {
                            inFlightRequests.remove(url)
                        }
                    }
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        return deferred.await() as T
    }
}
