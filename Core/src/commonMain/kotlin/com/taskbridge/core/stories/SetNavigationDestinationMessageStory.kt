package com.taskbridge.core.stories

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.navigation.NavigationDestinationMessage
import com.taskbridge.core.services.appstate.AppStateCommand

internal class SetNavigationDestinationMessageStory(
    private val assembler: CoreAssembler
) {
    suspend operator fun invoke(message: NavigationDestinationMessage?) {
        val appStateService = assembler.stories.getAppStateService(assembler)()
        appStateService.sendCommand(AppStateCommand.SetNavigationDestinationMessage(message))
    }
}
