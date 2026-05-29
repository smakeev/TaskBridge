package com.taskbridge.core.interactors.tasks

import com.taskbridge.core.composition.CoreAccess
import com.taskbridge.core.models.tasks.TaskId
import com.taskbridge.core.models.tasks.TaskItem
import com.taskbridge.core.usecases.tasks.TasksState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Platform-facing interactor for managing tasks.
 * Maps internal service data to public [TasksState].
 *
 * The [accessToken] parameter prevents construction outside the Core module:
 * only Core can mint a [CoreAccess] instance. The internal use case is
 * pulled from the token; its type is not exposed in this signature.
 */
public class TasksInteractor public constructor(
    accessToken: CoreAccess
) {
    private val useCase = accessToken.dependencies.tasksUseCase()
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
