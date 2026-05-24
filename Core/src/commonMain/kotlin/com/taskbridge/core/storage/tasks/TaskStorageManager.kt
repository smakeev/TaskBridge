package com.taskbridge.core.storage.tasks

import com.taskbridge.core.models.tasks.TaskId
import com.taskbridge.core.models.tasks.TaskItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Task-specific storage manager that hides Room details from the rest of the Core.
 * Ensures data consistency for multi-row operations by using DAO transactions.
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
     * Saves a single root task and replaces its entire subtree in a transaction.
     */
    suspend fun upsertTaskTree(rootTask: TaskItem) {
        upsertTaskTrees(listOf(rootTask))
    }

    /**
     * Saves a list of root tasks and replaces their respective subtrees in a single transaction.
     * Unrelated root tasks are preserved.
     */
    suspend fun upsertTaskTrees(rootTasks: List<TaskItem>) {
        val allEntities = taskDao.getAllTasks()
        
        // 1. Remove existing subtrees for provided roots to avoid orphans
        val idsToDelete = rootTasks.flatMap { root ->
            collectSubtreeIds(root.id.value, allEntities)
        }

        // 2. Map new trees to entities
        val newEntities = rootTasks.flatMap { it.toEntities() }

        // 3. Perform atomic replacement via DAO transaction
        taskDao.replaceSubtrees(idsToDelete, newEntities)
    }

    /**
     * Replaces ALL tasks in the database with the provided task trees in a transaction.
     */
    suspend fun replaceAllTasks(rootTasks: List<TaskItem>) {
        val allEntities = rootTasks.flatMap { it.toEntities() }
        taskDao.replaceAllTasks(allEntities)
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
        val allEntities = taskDao.getAllTasks()
        val idsToDelete = collectSubtreeIds(taskId.value, allEntities).toList()
        
        if (idsToDelete.isNotEmpty()) {
            taskDao.deleteTasksByIds(idsToDelete)
        }
    }

    /**
     * Recursively collects the ID of the root and all its descendants.
     */
    private fun collectSubtreeIds(rootId: String, allEntities: List<TaskEntity>): Set<String> {
        val groupedByParent = allEntities.groupBy { it.parentId }
        val result = mutableSetOf<String>()

        fun collect(id: String) {
            result.add(id)
            groupedByParent[id]?.forEach { child ->
                collect(child.id)
            }
        }

        if (allEntities.any { it.id == rootId }) {
            collect(rootId)
        }
        return result
    }
}
