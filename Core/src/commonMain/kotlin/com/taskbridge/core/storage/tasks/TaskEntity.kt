package com.taskbridge.core.storage.tasks

/**
 * Persistence entity representing a task.
 * Designed for flat relational storage (e.g., Room).
 */
internal data class TaskEntity(
    val id: String,
    val parentId: String?,
    val title: String,
    val type: String,
    val isDone: Boolean?,
    val progress: Int?,
    val sortOrder: Int,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
