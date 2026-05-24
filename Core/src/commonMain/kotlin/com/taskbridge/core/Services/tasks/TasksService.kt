package com.taskbridge.core.services.tasks

import com.taskbridge.core.services.common.BaseStatefulService
import com.taskbridge.core.storage.tasks.TaskStorageManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope

/**
 * Stateful service for managing tasks.
 * Owns the task tree state and persists changes through [TaskStorageManager].
 * Reloads state from storage after mutations to ensure deterministic behavior.
 */
internal class TasksService(
    scope: CoroutineScope,
    private val storageManager: TaskStorageManager
) : BaseStatefulService<TasksCommand, TasksServiceData>(
    initialData = TasksServiceData(),
    scope = scope
) {

    override suspend fun handleCommand(command: TasksCommand) {
        when (command) {
            is TasksCommand.LoadTasks -> performLoad()
            is TasksCommand.CreateTask -> performMutation {
                storageManager.upsertTaskTree(command.task)
            }
            is TasksCommand.ReplaceTask -> performMutation {
                storageManager.upsertTaskTree(command.task)
            }
            is TasksCommand.DeleteTaskTree -> performMutation {
                storageManager.deleteTaskTree(command.taskId)
            }
        }
    }

    private suspend fun performLoad() {
        updateState { it.copy(isLoading = true, errorMessage = null) }
        try {
            val loadedTasks = storageManager.loadAllTasks()
            updateState { 
                it.copy(
                    tasks = loadedTasks,
                    isLoading = false,
                    errorMessage = null
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            updateState { 
                it.copy(
                    isLoading = false, 
                    errorMessage = e.message ?: e::class.simpleName ?: "Unknown error"
                )
            }
        }
    }

    private suspend fun performMutation(action: suspend () -> Unit) {
        updateState { it.copy(isLoading = true, errorMessage = null) }
        try {
            action()
            // Reload from storage after successful mutation
            val loadedTasks = storageManager.loadAllTasks()
            updateState { 
                it.copy(
                    tasks = loadedTasks,
                    isLoading = false,
                    errorMessage = null
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            updateState { 
                it.copy(
                    isLoading = false, 
                    errorMessage = e.message ?: e::class.simpleName ?: "Unknown error"
                )
            }
        }
    }
}
