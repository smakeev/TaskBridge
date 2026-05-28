package com.taskbridge.android.repository

import com.taskbridge.android.repository.impl.MessagesRepositoryImpl
import com.taskbridge.android.repository.impl.NavigationRepositoryImpl
import com.taskbridge.android.repository.impl.RemindersRepositoryImpl
import com.taskbridge.android.repository.impl.TaskTemplatesRepositoryImpl
import com.taskbridge.android.repository.impl.TasksRepositoryImpl
import com.taskbridge.core.TaskBridge
import java.lang.ref.WeakReference
import kotlinx.coroutines.CoroutineScope

class RepositoriesStorage(
    private val taskBridge: TaskBridge,
    private val scope: CoroutineScope
) {
    private var navigationRepositoryRef: WeakReference<NavigationRepository>? = null
    private var taskTemplatesRepositoryRef: WeakReference<TaskTemplatesRepository>? = null
    private var tasksRepositoryRef: WeakReference<TasksRepository>? = null
    private var remindersRepositoryRef: WeakReference<RemindersRepository>? = null
    private var messagesRepositoryRef: WeakReference<MessagesRepository>? = null

    fun navigationRepository(): NavigationRepository {
        navigationRepositoryRef?.get()?.let { return it }
        return NavigationRepositoryImpl(taskBridge.navigationInteractor())
            .also { navigationRepositoryRef = WeakReference(it) }
    }

    fun taskTemplatesRepository(): TaskTemplatesRepository {
        taskTemplatesRepositoryRef?.get()?.let { return it }
        return TaskTemplatesRepositoryImpl(taskBridge.templatesInteractor())
            .also { taskTemplatesRepositoryRef = WeakReference(it) }
    }

    fun tasksRepository(): TasksRepository {
        tasksRepositoryRef?.get()?.let { return it }
        return TasksRepositoryImpl(taskBridge.tasksInteractor())
            .also { tasksRepositoryRef = WeakReference(it) }
    }

    fun remindersRepository(): RemindersRepository {
        remindersRepositoryRef?.get()?.let { return it }
        return RemindersRepositoryImpl(taskBridge.remindersInteractor(), scope)
            .also { remindersRepositoryRef = WeakReference(it) }
    }

    fun messagesRepository(): MessagesRepository {
        messagesRepositoryRef?.get()?.let { return it }
        return MessagesRepositoryImpl(taskBridge.messagesInteractor())
            .also { messagesRepositoryRef = WeakReference(it) }
    }
}
