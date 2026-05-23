package com.taskbridge.core.composition

/**
 * Internal composition root.
 * Owns the service locator and containers.
 */
internal class CoreAssembler {
    val services = CoreServiceLocator()
    val stories = UserStoriesContainer()
    val useCases = UseCaseContainer()
}
