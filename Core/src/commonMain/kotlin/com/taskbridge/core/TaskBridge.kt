package com.taskbridge.core

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.interactors.navigation.NavigationInteractor
import kotlin.reflect.KClass

/**
 * Main entry point for the Core module.
 */
public class TaskBridge {

    public companion object {
        public const val TEMPLATES_URL: String = "https://raw.githubusercontent.com/smakeev/TaskBridge/main/docs/mock-api/templates.json"
    }

    private val assembler = CoreAssembler()

    /**
     * Provides access to the navigation interactor.
     */
    public fun navigationInteractor(): NavigationInteractor {
        return assembler.navigationInteractor()
    }

    /**
     * Retrieves a use case by its type. Internal to Core.
     */
    internal fun <T : Any> getUseCase(type: KClass<T>): T {
        return assembler.useCases.get(assembler, type)
    }

    constructor()
}
