package com.taskbridge.android.repository

import com.taskbridge.android.repository.impl.MessagesRepositoryImpl
import com.taskbridge.android.repository.impl.NavigationRepositoryImpl
import com.taskbridge.android.repository.impl.RemindersRepositoryImpl
import com.taskbridge.android.repository.impl.TaskTemplatesRepositoryImpl
import com.taskbridge.android.repository.impl.TasksRepositoryImpl
import com.taskbridge.core.TaskBridge
import com.taskbridge.core.composition.CoreRepositoryAssembler
import kotlinx.coroutines.CoroutineScope

class RepositoriesStorage private constructor(
    private val interactors: CoreRepositoryAssembler,
    private val scope: CoroutineScope
) {
    val navigationRepository: NavigationRepository by lazy {
        NavigationRepositoryImpl(interactors.navigationInteractor())
    }

    val taskTemplatesRepository: TaskTemplatesRepository by lazy {
        TaskTemplatesRepositoryImpl(interactors.templatesInteractor())
    }

    val tasksRepository: TasksRepository by lazy {
        TasksRepositoryImpl(interactors.tasksInteractor())
    }

    val remindersRepository: RemindersRepository by lazy {
        RemindersRepositoryImpl(interactors.remindersInteractor(), scope)
    }

    val messagesRepository: MessagesRepository by lazy {
        MessagesRepositoryImpl(interactors.messagesInteractor())
    }

    companion object {
        fun create(taskBridge: TaskBridge, scope: CoroutineScope): RepositoriesStorage {
            return taskBridge.bootstrap { interactors ->
                RepositoriesStorage(interactors, scope)
            }
        }
    }
}
