package com.taskbridge.core.composition

import com.taskbridge.core.usecases.NavigationStateObserverUseCase
import com.taskbridge.core.usecases.PushNavigationUseCase
import com.taskbridge.core.usecases.SelectTabUseCase
import com.taskbridge.core.usecases.messages.MessagesUseCase
import com.taskbridge.core.usecases.reminders.RemindersUseCase
import com.taskbridge.core.usecases.tasks.TasksUseCase
import com.taskbridge.core.usecases.templates.TaskTemplatesUseCase

/**
 * Capability token required to construct any public Core interactor.
 *
 * The constructor is `internal`, so only Core itself can mint a [CoreAccess]
 * instance. Platform code may name the type (it appears in interactor
 * constructor signatures) but cannot create one — which makes it impossible
 * to instantiate an interactor outside the Core module's bootstrap path.
 *
 * The token also carries the internal dependency bundle each interactor needs.
 * Because [dependencies] and its type are `internal`, the bundle is invisible
 * outside the Core module; the public interactor constructor signature stays
 * clean (`(accessToken: CoreAccess)`) without exposing any internal types.
 *
 * The bundle holds use-case *factories* (`() -> UseCase`), not instances, so the
 * token itself is a stateless, shareable bundle of constructors. Each interactor
 * calls the factory it needs once and owns the resulting use case, which keeps the
 * whole domain layer above the services on-demand: a use case lives as long as the
 * interactor that built it, which lives as long as the repository that built it.
 * Only the services (the AppState boxes) are long-lived singletons.
 *
 * This is the iOS-friendly counterpart to making the interactor constructors
 * themselves `internal`: Kotlin/Native exports `internal` declarations into
 * the Obj-C header with mangled names, so the token gate is the more
 * defensible boundary on that side.
 */
public class CoreAccess internal constructor(
    internal val dependencies: InteractorDependencies
)

internal class InteractorDependencies(
    val selectTabUseCase: () -> SelectTabUseCase,
    val pushNavigationUseCase: () -> PushNavigationUseCase,
    val navigationObserverUseCase: () -> NavigationStateObserverUseCase,
    val tasksUseCase: () -> TasksUseCase,
    val templatesUseCase: () -> TaskTemplatesUseCase,
    val remindersUseCase: () -> RemindersUseCase,
    val messagesUseCase: () -> MessagesUseCase
)
