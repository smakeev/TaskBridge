package com.taskbridge.core.services.common

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Abstract base class for stateless services.
 * Implements a sequential request processing loop using a mailbox pattern.
 */
public abstract class BaseStatelessService<R : ServiceRequest, T>(
    scope: CoroutineScope
) : StatelessService<R, T> {

    private val mailbox = Channel<Envelope<R, T>>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (envelope in mailbox) {
                try {
                    val result = handleRequest(envelope.request)
                    envelope.deferred.complete(result)
                } catch (e: Throwable) {
                    envelope.deferred.completeExceptionally(e)
                }
            }
        }
    }

    override suspend fun execute(request: R): T {
        val deferred = CompletableDeferred<T>()
        mailbox.send(Envelope(request, deferred))
        return deferred.await()
    }

    /**
     * Implementation-specific request handling logic.
     * Called sequentially for each request.
     */
    protected abstract suspend fun handleRequest(request: R): T

    /**
     * Internal envelope for grouping a request with its response deferred.
     */
    private data class Envelope<R : ServiceRequest, T>(
        val request: R,
        val deferred: CompletableDeferred<T>
    )
}
