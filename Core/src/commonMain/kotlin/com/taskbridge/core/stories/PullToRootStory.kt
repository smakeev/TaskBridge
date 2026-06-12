package com.taskbridge.core.stories

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.services.appstate.AppStateCommand

internal class PullToRootStory(
    private val assembler: CoreAssembler
) {
    suspend operator fun invoke(tab: AppTab) {
        val appStateService = assembler.services.appStateService()
        appStateService.sendCommand(AppStateCommand.PullToRoot(tab))
    }
}
