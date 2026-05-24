package com.taskbridge.core.stories

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.navigation.NavigationDestination
import com.taskbridge.core.services.appstate.AppStateCommand

internal class PushDestinationStory(
    private val assembler: CoreAssembler
) {
    suspend operator fun invoke(destination: NavigationDestination) {
        val appStateService = assembler.stories.getAppStateService(assembler)()
        appStateService.sendCommand(AppStateCommand.PushDestination(destination))
    }
}
