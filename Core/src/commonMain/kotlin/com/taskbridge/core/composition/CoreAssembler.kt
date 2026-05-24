package com.taskbridge.core.composition

import com.taskbridge.core.interactors.navigation.NavigationInteractor
import com.taskbridge.core.interactors.templates.TemplatesInteractor
import com.taskbridge.core.usecases.NavigationStateObserverUseCase
import com.taskbridge.core.usecases.SelectTabUseCase
import com.taskbridge.core.usecases.templates.TaskTemplatesUseCase

/**
 * Internal composition root.
 * Owns the service locator, containers, and provides interactor assembly.
 */
internal class CoreAssembler {
    val services = CoreServiceLocator()
    val stories = UserStoriesContainer()
    val useCases = UseCaseContainer()

    fun navigationInteractor(): NavigationInteractor {
        return NavigationInteractor(
            selectTabUseCase = useCases.get(this, SelectTabUseCase::class),
            observerUseCase = useCases.get(this, NavigationStateObserverUseCase::class)
        )
    }

    fun templatesInteractor(): TemplatesInteractor {
        return TemplatesInteractor(
            useCase = useCases.get(this, TaskTemplatesUseCase::class)
        )
    }
}
