package com.taskbridge.core.services.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Abstract base class for stateless services.
 * Implements a concurrent request processing loop using a mailbox pattern.
 */
internal abstract class BaseStatelessService<R : ServiceRequest, T>(
    private val scope: CoroutineScope
) : StatelessService<R, T> {

    private val mailbox = Channel<Envelope<R, T>>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (envelope in mailbox) {
                if (envelope.deferred.isCancelled) {
                    continue
                }

                val job = scope.launch {
                    try {
                        val result = handleRequest(envelope.request)
                        envelope.deferred.complete(result)
                    } catch (e: Throwable) {
                        envelope.deferred.completeExceptionally(e)
                    }
                }

                envelope.deferred.invokeOnCompletion { throwable ->
                    if (throwable is CancellationException) {
                        job.cancel()
                    }
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
     * Called concurrently for each request received from the mailbox.
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
