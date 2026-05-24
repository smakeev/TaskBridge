package com.taskbridge.core.storage.tasks

import androidx.room.useWriterConnection
import com.taskbridge.core.models.tasks.TaskId
import com.taskbridge.core.models.tasks.TaskItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Task-specific storage manager that hides Room details from the rest of the Core.
 * Multi-row tree operations use transactions to ensure consistency.
 */
internal class TaskStorageManager(
    private val database: TaskDatabase
) {
    private val taskDao get() = database.taskDao()

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
     * Saves a single root task and all its descendants in a transaction.
     */
    suspend fun upsertTaskTree(rootTask: TaskItem) {
        upsertTaskTrees(listOf(rootTask))
    }

    /**
     * Saves a list of root tasks and all their descendants in a transaction.
     */
    suspend fun upsertTaskTrees(rootTasks: List<TaskItem>) {
        database.useWriterConnection {
            val allEntities = rootTasks.flatMap { it.toEntities() }
            taskDao.upsertTasks(allEntities)
        }
    }

    /**
     * Replaces all tasks in the database with the provided task trees in a transaction.
     */
    suspend fun replaceAllTasks(rootTasks: List<TaskItem>) {
        database.useWriterConnection {
            val allEntities = rootTasks.flatMap { it.toEntities() }
            taskDao.replaceAllTasks(allEntities)
        }
    }

    /**
     * Deletes a specific task by its ID.
     */
    suspend fun deleteTask(taskId: TaskId) {
        taskDao.deleteTaskById(taskId.value)
    }

    /**
     * Deletes an entire task subtree starting from the given [taskId].
     * Subtree deletion is explicit to avoid depending on SQLite cascade configuration behavior.
     */
    suspend fun deleteTaskTree(taskId: TaskId) {
        database.useWriterConnection {
            // 1. Load all entities to build lookup
            val allEntities = taskDao.getAllTasks()
            val groupedByParent = allEntities.groupBy { it.parentId }
            
            // 2. Recursively collect all descendant IDs
            val idsToDelete = mutableListOf<String>()
            
            fun collectIds(id: String) {
                idsToDelete.add(id)
                groupedByParent[id]?.forEach { child ->
                    collectIds(child.id)
                }
            }
            
            // Start collection if the target task exists
            if (allEntities.any { it.id == taskId.value }) {
                collectIds(taskId.value)
            }

            // 3. Delete all collected IDs in one transaction
            if (idsToDelete.isNotEmpty()) {
                taskDao.deleteTasksByIds(idsToDelete)
            }
        }
    }
}
