package com.taskbridge.core.stories

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.services.appstate.AppStateCommand

internal class PopDestinationStory(
    private val assembler: CoreAssembler
) {
    suspend operator fun invoke() {
        val appStateService = assembler.services.appStateService()
        appStateService.sendCommand(AppStateCommand.PopDestination)
    }
}
