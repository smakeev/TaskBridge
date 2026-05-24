package com.taskbridge.core.usecases.tasks

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.tasks.TaskId
import com.taskbridge.core.models.tasks.TaskItem
import com.taskbridge.core.services.tasks.TasksServiceData
import kotlinx.coroutines.flow.Flow

/**
 * Internal use case for managing tasks.
 * Orchestrates task stories and provides access to internal task data.
 */
public class TasksUseCase internal constructor(
    private val assembler: CoreAssembler
) {
    public suspend fun loadTasks() {
        assembler.stories.loadTasks(assembler).loadTasks()
    }

    public suspend fun createTask(task: TaskItem) {
        assembler.stories.createTask(assembler).createTask(task)
    }

    public suspend fun replaceTask(task: TaskItem) {
        assembler.stories.replaceTask(assembler).replaceTask(task)
    }

    public suspend fun deleteTaskTree(taskId: TaskId) {
        assembler.stories.deleteTaskTree(assembler).deleteTaskTree(taskId)
    }

    /**
     * Provides access to the raw internal task service data flow.
     */
    internal fun observeTasks(): Flow<TasksServiceData> {
        return assembler.stories.observeTasks(assembler).observeTasks()
    }
}
