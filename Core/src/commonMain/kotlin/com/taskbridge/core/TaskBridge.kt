package com.taskbridge.core

import com.taskbridge.core.composition.CoreAssembler
import kotlin.reflect.KClass

/**
 * Main entry point for the Core module.
 */
public class TaskBridge {

    private val assembler = CoreAssembler()

    /**
     * Retrieves a use case by its type.
     */
    public fun <T : Any> getUseCase(type: KClass<T>): T {
        return assembler.useCases.get(assembler, type)
    }

    constructor()
}
