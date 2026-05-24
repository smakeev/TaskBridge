package com.taskbridge.android.repository

import com.taskbridge.core.models.templates.TaskTemplatesState
import kotlinx.coroutines.flow.Flow

interface TaskTemplatesRepository {
    val templatesState: Flow<TaskTemplatesState>
    suspend fun loadTemplates()
    suspend fun forceLoadTemplates()
}
