package com.taskbridge.core.models.templates

import com.taskbridge.core.models.templates.TaskTemplate

/**
 * Public state model for task templates.
 */
public data class TaskTemplatesState(
    val templates: List<TaskTemplate> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastLoadedAtMillis: Long? = null
)
