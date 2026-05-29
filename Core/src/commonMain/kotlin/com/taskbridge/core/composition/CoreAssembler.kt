package com.taskbridge.core.composition

import com.taskbridge.core.events.CoreEventBuses
import com.taskbridge.core.events.CoreEventEmitter
import com.taskbridge.core.handlers.CorePlatformHandlers
import com.taskbridge.core.interactors.messages.MessagesInteractor
import com.taskbridge.core.interactors.navigation.NavigationInteractor
import com.taskbridge.core.interactors.reminders.RemindersInteractor
import com.taskbridge.core.interactors.tasks.TasksInteractor
import com.taskbridge.core.interactors.templates.TemplatesInteractor
import com.taskbridge.core.storage.tasks.PlatformDependencies
import com.taskbridge.core.usecases.NavigationStateObserverUseCase
import com.taskbridge.core.usecases.PushNavigationUseCase
import com.taskbridge.core.usecases.SelectTabUseCase
import com.taskbridge.core.usecases.messages.MessagesUseCase
import com.taskbridge.core.usecases.reminders.RemindersUseCase
import com.taskbridge.core.usecases.tasks.TasksUseCase
import com.taskbridge.core.usecases.templates.TaskTemplatesUseCase

/**
 * Internal composition root.
 * Owns the service locator, containers, and provides interactor assembly.
 */
internal class CoreAssembler(
    val platformDependencies: PlatformDependencies,
    val platformHandlers: CorePlatformHandlers
) {
    val buses = CoreEventBuses()
    val eventEmitter = CoreEventEmitter(buses)
    val services = CoreServiceLocator(
        platformDependencies = platformDependencies,
        reminderHandler = platformHandlers.reminderHandler,
        reminderEvents = buses.reminderEvents
    )
    val stories = UserStoriesContainer()
    val useCases = UseCaseContainer()

    // The token carries use-case *factories*, not instances, so it holds no domain
    // state and is safe to share. `useCases.get(...)` already builds a fresh use case
    // per call, so each interactor that calls a factory owns its own use case.
    private val coreAccess: CoreAccess by lazy {
        CoreAccess(
            dependencies = InteractorDependencies(
                selectTabUseCase = { useCases.get(this, SelectTabUseCase::class) },
                pushNavigationUseCase = { useCases.get(this, PushNavigationUseCase::class) },
                navigationObserverUseCase = { useCases.get(this, NavigationStateObserverUseCase::class) },
                tasksUseCase = { useCases.get(this, TasksUseCase::class) },
                templatesUseCase = { useCases.get(this, TaskTemplatesUseCase::class) },
                remindersUseCase = { useCases.get(this, RemindersUseCase::class) },
                messagesUseCase = { useCases.get(this, MessagesUseCase::class) }
            )
        )
    }

    // Interactors are built fresh on every call. They are stateless wrappers (their
    // flow properties are cold definitions with no construction-time side effects),
    // and the platform repository that calls the factory owns the result — so an
    // interactor (and the use cases/stories it builds) lives only as long as the
    // repository needs it, while the underlying services stay singletons.
    fun coreRepositoryAssembler(): CoreRepositoryAssembler {
        return CoreRepositoryAssembler(
            navigationInteractor = { NavigationInteractor(coreAccess) },
            tasksInteractor = { TasksInteractor(coreAccess) },
            templatesInteractor = { TemplatesInteractor(coreAccess) },
            remindersInteractor = { RemindersInteractor(coreAccess) },
            messagesInteractor = { MessagesInteractor(coreAccess) }
        )
    }
}
