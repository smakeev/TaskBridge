package com.taskbridge.core.storage.tasks

import com.taskbridge.core.models.tasks.TaskId
import com.taskbridge.core.models.tasks.TaskItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Task-specific storage manager that hides Room details from the rest of the Core.
 */
internal class TaskStorageManager(
    private val taskDao: TaskDao
) {
    /**
     * Loads all tasks and reconstructs the domain tree.
     */
    suspend fun loadAllTasks(): List<TaskItem> {
        val entities = taskDao.getAllTasks()
        return entities.toTaskTree()
    }

    /**
     * Observes all tasks and reconstructs the domain tree reactively.
     */
    fun observeAllTasks(): Flow<List<TaskItem>> {
        return taskDao.observeAllTasks().map { entities ->
            entities.toTaskTree()
        }
    }

    /**
     * Saves a list of root tasks, including all their descendants.
     */
    suspend fun saveTaskTree(rootTasks: List<TaskItem>) {
        val allEntities = rootTasks.flatMap { it.toEntities() }
        taskDao.upsertTasks(allEntities)
    }

    /**
     * Replaces all tasks in the database with the provided task trees.
     */
    suspend fun replaceAllTasks(rootTasks: List<TaskItem>) {
        val allEntities = rootTasks.flatMap { it.toEntities() }
        taskDao.replaceAllTasks(allEntities)
    }

    /**
     * Deletes a specific task by its ID.
     * TODO: Decide if deletion should cascade to children or if Room's ForeignKey should handle it.
     */
    suspend fun deleteTask(id: TaskId) {
        taskDao.deleteTaskById(id.value)
    }
}
