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
internal class TasksUseCase(
    private val assembler: CoreAssembler
) {
    suspend fun loadTasks() {
        assembler.stories.loadTasks(assembler).loadTasks()
    }

    suspend fun createTask(task: TaskItem) {
        assembler.stories.createTask(assembler).createTask(task)
    }

    suspend fun replaceTask(task: TaskItem) {
        assembler.stories.replaceTask(assembler).replaceTask(task)
    }

    suspend fun deleteTaskTree(taskId: TaskId) {
        assembler.stories.deleteTaskTree(assembler).deleteTaskTree(taskId)
    }

    /**
     * Provides access to the raw internal task service data flow.
     */
    fun observeTasks(): Flow<TasksServiceData> {
        return assembler.stories.observeTasks(assembler).observeTasks()
    }
}
