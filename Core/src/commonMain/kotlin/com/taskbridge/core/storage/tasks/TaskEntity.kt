package com.taskbridge.core.storage.tasks

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persistence entity representing a task.
 * Designed for flat relational storage (e.g., Room).
 */
@Entity(
    tableName = "tasks",
    indices = [Index(value = ["parentId"])]
)
public data class TaskEntity(
    @PrimaryKey val id: String,
    val parentId: String?,
    val title: String,
    val type: String,
    val isDone: Boolean?,
    val progress: Int?,
    val sortOrder: Int,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
