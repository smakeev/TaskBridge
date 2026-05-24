package com.taskbridge.core.interactors.tasks

import com.taskbridge.core.models.tasks.TaskId
import com.taskbridge.core.models.tasks.TaskItem
import com.taskbridge.core.usecases.tasks.TasksState
import com.taskbridge.core.usecases.tasks.TasksUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Platform-facing interactor for managing tasks.
 * Maps internal service data to public [TasksState].
 */
public class TasksInteractor internal constructor(
    private val useCase: TasksUseCase
) {
    /**
     * Observable flow of the current tasks state.
     */
    public val tasksState: Flow<TasksState> = useCase.observeTasks()
        .map { data ->
            TasksState(
                tasks = data.tasks,
                isLoading = data.isLoading,
                errorMessage = data.errorMessage
            )
        }
        .distinctUntilChanged()

    public suspend fun loadTasks() {
        useCase.loadTasks()
    }

    public suspend fun createTask(task: TaskItem) {
        useCase.createTask(task)
    }

    public suspend fun replaceTask(task: TaskItem) {
        useCase.replaceTask(task)
    }

    public suspend fun deleteTaskTree(taskId: TaskId) {
        useCase.deleteTaskTree(taskId)
    }
}
