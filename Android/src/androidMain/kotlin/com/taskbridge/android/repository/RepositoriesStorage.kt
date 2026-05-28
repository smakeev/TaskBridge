package com.taskbridge.android.repository

import com.taskbridge.android.repository.impl.MessagesRepositoryImpl
import com.taskbridge.android.repository.impl.NavigationRepositoryImpl
import com.taskbridge.android.repository.impl.RemindersRepositoryImpl
import com.taskbridge.android.repository.impl.TaskTemplatesRepositoryImpl
import com.taskbridge.android.repository.impl.TasksRepositoryImpl
import com.taskbridge.core.TaskBridge
import kotlinx.coroutines.CoroutineScope

class RepositoriesStorage(
    private val taskBridge: TaskBridge,
    private val scope: CoroutineScope
) {
    val navigationRepository: NavigationRepository by lazy {
        NavigationRepositoryImpl(taskBridge.navigationInteractor())
    }

    val taskTemplatesRepository: TaskTemplatesRepository by lazy {
        TaskTemplatesRepositoryImpl(taskBridge.templatesInteractor())
    }

    val tasksRepository: TasksRepository by lazy {
        TasksRepositoryImpl(taskBridge.tasksInteractor())
    }

    val remindersRepository: RemindersRepository by lazy {
        RemindersRepositoryImpl(taskBridge.remindersInteractor(), scope)
    }

    val messagesRepository: MessagesRepository by lazy {
        MessagesRepositoryImpl(taskBridge.messagesInteractor())
    }
}
