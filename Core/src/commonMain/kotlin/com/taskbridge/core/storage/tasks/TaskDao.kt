package com.taskbridge.core.storage.tasks

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY parentId, sortOrder")
    suspend fun getAllTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks ORDER BY parentId, sortOrder")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    @Upsert
    suspend fun upsertTasks(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("DELETE FROM tasks WHERE id IN (:ids)")
    suspend fun deleteTasksByIds(ids: List<String>)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Transaction
    suspend fun replaceAllTasks(tasks: List<TaskEntity>) {
        deleteAllTasks()
        upsertTasks(tasks)
    }

    @Transaction
    suspend fun replaceSubtrees(idsToDelete: List<String>, newEntities: List<TaskEntity>) {
        if (idsToDelete.isNotEmpty()) {
            deleteTasksByIds(idsToDelete)
        }
        upsertTasks(newEntities)
    }
}
