package com.taskbridge.core.services.appstate

import com.taskbridge.core.models.navigation.NavigationState
import com.taskbridge.core.services.common.BaseStatefulService
import kotlinx.coroutines.CoroutineScope

internal class AppStateService(
    scope: CoroutineScope
) : BaseStatefulService<AppStateCommand, AppStateServiceData>(
    initialData = AppStateServiceData(),
    scope = scope
) {

    override suspend fun handleCommand(
        currentData: AppStateServiceData,
        command: AppStateCommand
    ): AppStateServiceData {
        return when (command) {
            is AppStateCommand.SelectTab -> {
                currentData.copy(
                    navigationState = currentData.navigationState.selectTab(command.tab)
                )
            }
        }
    }

    private fun NavigationState.selectTab(
        tab: com.taskbridge.core.models.navigation.AppTab
    ): NavigationState {
        return copy(selectedTab = tab)
    }
}
