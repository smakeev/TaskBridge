package com.taskbridge.core.stories

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.services.appstate.AppStateService

/**
 * Story for retrieving the AppStateService.
 */
internal class GetAppStateServiceStory(
    private val assembler: CoreAssembler
) {
    operator fun invoke(): AppStateService {
        return assembler.services.appStateService()
    }
}
