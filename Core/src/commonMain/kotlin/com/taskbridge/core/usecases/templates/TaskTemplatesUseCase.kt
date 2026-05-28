package com.taskbridge.core.usecases.templates

import com.taskbridge.core.TaskBridge
import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.network.templates.dto.TaskTemplateDto
import com.taskbridge.core.services.remote.RemoteResourceEntry
import kotlinx.coroutines.flow.Flow

/**
 * Internal use case for managing task templates.
 * Orchestrates remote resource loading and observation.
 */
internal class TaskTemplatesUseCase(
    private val assembler: CoreAssembler
) {
    suspend fun loadTemplates() {
        assembler.stories.loadRemoteResource(assembler).load<List<TaskTemplateDto>>(
            url = TaskBridge.TEMPLATES_URL,
            ttlMillis = TaskBridge.TEMPLATES_TTL_MILLIS,
            autoRefreshEnabled = false
        )
    }

    suspend fun forceLoadTemplates() {
        assembler.stories.forceLoadRemoteResource(assembler).forceLoad<List<TaskTemplateDto>>(
            url = TaskBridge.TEMPLATES_URL,
            ttlMillis = TaskBridge.TEMPLATES_TTL_MILLIS,
            autoRefreshEnabled = false
        )
    }

    /**
     * Provides access to the raw remote resource flow.
     */
    fun observeTemplatesResource(): Flow<RemoteResourceEntry?> {
        return assembler.stories.observeRemoteResource(assembler).observe(TaskBridge.TEMPLATES_URL)
    }
}
