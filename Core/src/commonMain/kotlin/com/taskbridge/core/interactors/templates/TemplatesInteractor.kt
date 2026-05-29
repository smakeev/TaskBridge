package com.taskbridge.core.interactors.templates

import com.taskbridge.core.composition.CoreAccess
import com.taskbridge.core.models.templates.TaskTemplatesState
import com.taskbridge.core.network.templates.dto.TaskTemplateDto
import com.taskbridge.core.network.templates.dto.toDomain
import com.taskbridge.core.services.remote.RemoteResourceStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Platform-facing interactor for managing task templates.
 * Maps Core internal data (RemoteResourceEntry) to user-facing TaskTemplatesState.
 *
 * The [accessToken] parameter prevents construction outside the Core module:
 * only Core can mint a [CoreAccess] instance. The internal use case is
 * pulled from the token; its type is not exposed in this signature.
 */
public class TemplatesInteractor public constructor(
    accessToken: CoreAccess
) {
    private val useCase = accessToken.dependencies.templatesUseCase()
    /**
     * Observable flow of the current templates state.
     */
    public val templatesState: Flow<TaskTemplatesState> = useCase.observeTemplatesResource()
        .map { entry ->
            if (entry == null) {
                return@map TaskTemplatesState()
            }

            @Suppress("UNCHECKED_CAST")
            val dtos = entry.data as? List<TaskTemplateDto>
            val domainTemplates = dtos?.map { it.toDomain() }.orEmpty()

            TaskTemplatesState(
                templates = domainTemplates,
                isLoading = entry.status == RemoteResourceStatus.LOADING,
                errorMessage = entry.errorMessage,
                lastLoadedAtMillis = entry.loadedAtMillis
            )
        }

    /**
     * Triggers loading of templates if the current cache is expired.
     */
    public suspend fun loadTemplates() {
        useCase.loadTemplates()
    }

    /**
     * Forcefully reloads templates, ignoring the current cache state.
     */
    public suspend fun forceLoadTemplates() {
        useCase.forceLoadTemplates()
    }
}
