package com.taskbridge.core.stories

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.services.appstate.AppStateServiceData
import kotlinx.coroutines.flow.first

internal class FetchNavigationDestinationMessageStory(
    private val assembler: CoreAssembler
) {
    suspend operator fun invoke(): AppStateServiceData {
        val appStateService = assembler.stories.getAppStateService(assembler)()
        return appStateService.data.first()
    }
}
