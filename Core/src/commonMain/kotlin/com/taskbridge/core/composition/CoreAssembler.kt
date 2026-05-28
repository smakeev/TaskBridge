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

    private val coreAccess: CoreAccess by lazy {
        CoreAccess(
            dependencies = InteractorDependencies(
                selectTabUseCase = useCases.get(this, SelectTabUseCase::class),
                pushNavigationUseCase = useCases.get(this, PushNavigationUseCase::class),
                navigationObserverUseCase = useCases.get(this, NavigationStateObserverUseCase::class),
                tasksUseCase = useCases.get(this, TasksUseCase::class),
                templatesUseCase = useCases.get(this, TaskTemplatesUseCase::class),
                remindersUseCase = useCases.get(this, RemindersUseCase::class),
                messagesUseCase = useCases.get(this, MessagesUseCase::class)
            )
        )
    }

    // Lazy interactor instances. Each is built on first access and reused
    // thereafter. They live on the assembler so the same TaskBridge always
    // hands out the same interactor instances regardless of how many times
    // [coreRepositoryAssembler] is invoked.
    private val lazyNavigationInteractor: Lazy<NavigationInteractor> =
        lazy { NavigationInteractor(coreAccess) }
    private val lazyTasksInteractor: Lazy<TasksInteractor> =
        lazy { TasksInteractor(coreAccess) }
    private val lazyTemplatesInteractor: Lazy<TemplatesInteractor> =
        lazy { TemplatesInteractor(coreAccess) }
    private val lazyRemindersInteractor: Lazy<RemindersInteractor> =
        lazy { RemindersInteractor(coreAccess) }
    private val lazyMessagesInteractor: Lazy<MessagesInteractor> =
        lazy { MessagesInteractor(coreAccess) }

    fun coreRepositoryAssembler(): CoreRepositoryAssembler {
        return CoreRepositoryAssembler(
            navigationInteractor = { lazyNavigationInteractor.value },
            tasksInteractor = { lazyTasksInteractor.value },
            templatesInteractor = { lazyTemplatesInteractor.value },
            remindersInteractor = { lazyRemindersInteractor.value },
            messagesInteractor = { lazyMessagesInteractor.value }
        )
    }
}
