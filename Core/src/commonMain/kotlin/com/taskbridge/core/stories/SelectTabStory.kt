package com.taskbridge.core.stories

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.services.appstate.AppStateCommand

/**
 * Story for selecting a tab.
 * Uses the GetAppStateServiceStory to access the service.
 */
internal class SelectTabStory(
    private val assembler: CoreAssembler
) {
    suspend operator fun invoke(tab: AppTab) {
        val appStateService = assembler.services.appStateService()
        appStateService.sendCommand(AppStateCommand.SelectTab(tab))
    }
}
