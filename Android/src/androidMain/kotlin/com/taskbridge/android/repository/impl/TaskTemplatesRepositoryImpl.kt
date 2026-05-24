package com.taskbridge.android.repository.impl

import com.taskbridge.android.repository.TaskTemplatesRepository
import com.taskbridge.core.interactors.templates.TemplatesInteractor
import com.taskbridge.core.models.templates.TaskTemplatesState
import kotlinx.coroutines.flow.Flow

class TaskTemplatesRepositoryImpl(
    private val interactor: TemplatesInteractor
) : TaskTemplatesRepository {
    override val templatesState: Flow<TaskTemplatesState> = interactor.templatesState

    override suspend fun loadTemplates() {
        interactor.loadTemplates()
    }

    override suspend fun forceLoadTemplates() {
        interactor.forceLoadTemplates()
    }
}
