package com.taskbridge.android.repository.impl

import com.taskbridge.android.repository.TasksRepository
import com.taskbridge.core.interactors.tasks.TasksInteractor
import com.taskbridge.core.models.tasks.TaskId
import com.taskbridge.core.models.tasks.TaskItem
import com.taskbridge.core.usecases.tasks.TasksState
import kotlinx.coroutines.flow.Flow

class TasksRepositoryImpl(
    private val interactor: TasksInteractor
) : TasksRepository {
    override val tasksState: Flow<TasksState> = interactor.tasksState

    override suspend fun loadTasks() {
        interactor.loadTasks()
    }

    override suspend fun createTask(task: TaskItem) {
        interactor.createTask(task)
    }

    override suspend fun replaceTask(task: TaskItem) {
        interactor.replaceTask(task)
    }

    override suspend fun deleteTaskTree(taskId: TaskId) {
        interactor.deleteTaskTree(taskId)
    }
}
