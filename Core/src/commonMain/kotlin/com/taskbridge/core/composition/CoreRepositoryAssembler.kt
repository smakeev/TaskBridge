package com.taskbridge.core.composition

import com.taskbridge.core.interactors.messages.MessagesInteractor
import com.taskbridge.core.interactors.navigation.NavigationInteractor
import com.taskbridge.core.interactors.reminders.RemindersInteractor
import com.taskbridge.core.interactors.tasks.TasksInteractor
import com.taskbridge.core.interactors.templates.TemplatesInteractor

/**
 * Single entry point that hands platform code lazy factory closures for each
 * Core interactor. Construct via [com.taskbridge.core.TaskBridge.bootstrap].
 *
 * Each property is a `() -> Interactor` closure that, when invoked, returns
 * the lazily-built (and memoized) interactor instance. No interactor is
 * constructed until its closure is called for the first time; subsequent
 * calls return the same instance.
 *
 * Platforms typically wire these closures into their own lazy repository
 * storage (`by lazy` on Android, `weak` + create-if-nil on iOS) so a
 * repository — and the interactor that backs it — only comes into existence
 * the first time the platform actually needs it.
 */
public class CoreRepositoryAssembler internal constructor(
    public val navigationInteractor: () -> NavigationInteractor,
    public val tasksInteractor: () -> TasksInteractor,
    public val templatesInteractor: () -> TemplatesInteractor,
    public val remindersInteractor: () -> RemindersInteractor,
    public val messagesInteractor: () -> MessagesInteractor
)
