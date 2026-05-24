package com.taskbridge.core.composition

import com.taskbridge.core.usecases.NavigationStateObserverUseCase
import com.taskbridge.core.usecases.SelectTabUseCase
import com.taskbridge.core.usecases.tasks.TasksUseCase
import com.taskbridge.core.usecases.templates.TaskTemplatesUseCase
import kotlin.reflect.KClass

/**
 * Container for public use cases.
 * Provides generic access by type.
 */
internal class UseCaseContainer {

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(assembler: CoreAssembler, type: KClass<T>): T {
        return when (type) {
            SelectTabUseCase::class -> SelectTabUseCase(assembler) as T
            NavigationStateObserverUseCase::class -> NavigationStateObserverUseCase(assembler) as T
            TaskTemplatesUseCase::class -> TaskTemplatesUseCase(assembler) as T
            TasksUseCase::class -> TasksUseCase(assembler) as T
            else -> error("Unknown use case type: ${type.simpleName}")
        }
    }
}
