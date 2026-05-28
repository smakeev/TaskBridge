package com.taskbridge.core

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.composition.CoreRepositoryAssembler
import com.taskbridge.core.events.CoreEventEmitter
import com.taskbridge.core.handlers.CorePlatformHandlers
import com.taskbridge.core.storage.tasks.PlatformDependencies
import kotlin.reflect.KClass

/**
 * Main entry point for the Core module.
 */
public class TaskBridge(
    platformDependencies: PlatformDependencies,
    platformHandlers: CorePlatformHandlers
) {

    public companion object {
        public const val TEMPLATES_URL: String = "https://raw.githubusercontent.com/smakeev/TaskBridge/main/docs/mock-api/templates.json"

        /**
         * TTL for task templates.
         * TODO: In production this value could come from remote config, backend metadata, or feature flags.
         */
        public const val TEMPLATES_TTL_MILLIS: Long = 10 * 60 * 1000L // 10 minutes
    }

    private val assembler = CoreAssembler(platformDependencies, platformHandlers)

    init {
        // Wire the event emitter into platform handlers
        platformHandlers.reminderHandler.setEventEmitter(assembler.eventEmitter)
    }

    /**
     * Provides access to the event emitter for the platform layer.
     */
    public val eventEmitter: CoreEventEmitter
        get() = assembler.eventEmitter

    /**
     * One-shot bootstrap entry point.
     *
     * Core constructs every public interactor and hands them to [builder] inside
     * a [CoreRepositoryAssembler] scope. The platform is expected to wire each
     * interactor into its repository implementation inside the lambda and return
     * the resulting repositories container.
     *
     * Repositories may then hold their interactor as `private val ...` for the
     * lifetime of the app; the [CoreRepositoryAssembler] itself is intended to
     * be discarded once `builder` returns.
     *
     * No individual `xxxInteractor()` getters are exposed on this class — this
     * is the only path by which platform code can obtain an interactor instance.
     */
    public fun <T : Any> bootstrap(builder: (CoreRepositoryAssembler) -> T): T {
        return builder(assembler.coreRepositoryAssembler())
    }

    /**
     * Retrieves a use case by its type. Internal to Core.
     */
    internal fun <T : Any> getUseCase(type: KClass<T>): T {
        return assembler.useCases.get(assembler, type)
    }
}
