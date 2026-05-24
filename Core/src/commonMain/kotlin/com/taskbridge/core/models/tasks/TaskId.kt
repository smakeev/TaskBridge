package com.taskbridge.core.models.tasks

/**
 * Data class representing a unique identifier for a Task.
 * Using data class instead of value class for better Swift interoperability.
 */
public data class TaskId(
    val value: String
)
