package com.taskbridge.core

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.events.CoreEventEmitter
import com.taskbridge.core.handlers.CorePlatformHandlers
import com.taskbridge.core.interactors.navigation.NavigationInteractor
import com.taskbridge.core.interactors.tasks.TasksInteractor
import com.taskbridge.core.interactors.templates.TemplatesInteractor
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
     * Provides access to the navigation interactor.
     */
    public fun navigationInteractor(): NavigationInteractor {
        return assembler.navigationInteractor()
    }

    /**
     * Provides access to the templates interactor.
     */
    public fun templatesInteractor(): TemplatesInteractor {
        return assembler.templatesInteractor()
    }

    /**
     * Provides access to the tasks interactor.
     */
    public fun tasksInteractor(): TasksInteractor {
        return assembler.tasksInteractor()
    }

    /**
     * Retrieves a use case by its type. Internal to Core.
     */
    internal fun <T : Any> getUseCase(type: KClass<T>): T {
        return assembler.useCases.get(assembler, type)
    }
}
