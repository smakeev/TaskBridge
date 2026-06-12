package com.taskbridge.core.services.appstate

import com.taskbridge.core.models.navigation.NavigationDestinationMessage
import com.taskbridge.core.models.navigation.NavigationState
import com.taskbridge.core.services.common.BaseStatefulService
import kotlinx.coroutines.CoroutineScope

internal class AppStateService(
    scope: CoroutineScope
) : BaseStatefulService<AppStateCommand, AppStateServiceData>(
    initialData = AppStateServiceData(),
    scope = scope
) {

    override suspend fun handleCommand(command: AppStateCommand) {
        when (command) {
            is AppStateCommand.SelectTab -> {
                updateState { currentData ->
                    currentData.copy(
                        navigationState = currentData.navigationState.selectTab(command.tab)
                    )
                }
            }
            is AppStateCommand.PushDestination -> {
                updateState { currentData ->
                    currentData.copy(
                        navigationState = currentData.navigationState.push(command.destination)
                    )
                }
            }
            AppStateCommand.PopDestination -> {
                updateState { currentData ->
                    currentData.copy(
                        navigationState = currentData.navigationState.pop()
                    )
                }
            }
            is AppStateCommand.PullToRoot -> {
                updateState { currentData ->
                    currentData.copy(
                        navigationState = currentData.navigationState.pullToRoot(command.tab)
                    )
                }
            }
            is AppStateCommand.SetNavigationDestinationMessage -> {
                updateState { currentData ->
                    currentData.copy(navigationDestinationMessage = command.message)
                }
            }
            is AppStateCommand.ConsumeNavigationDestinationMessage -> {
                // Read-check-clear in one reducer step so no concurrent set/consume can
                // slip between the read and the clear (Core-3). The command loop is the
                // sole writer, so the captured value reflects the committed state.
                var consumed: NavigationDestinationMessage? = null
                updateState { currentData ->
                    val message = currentData.navigationDestinationMessage
                    if (message != null && message.scopeId == command.scopeId) {
                        consumed = message
                        currentData.copy(navigationDestinationMessage = null)
                    } else {
                        consumed = null
                        currentData
                    }
                }
                command.result.complete(consumed)
            }
        }
    }

    private fun NavigationState.selectTab(
        tab: com.taskbridge.core.models.navigation.AppTab
    ): NavigationState {
        return copy(selectedTab = tab)
    }

    private fun NavigationState.push(
        destination: com.taskbridge.core.models.navigation.NavigationDestination
    ): NavigationState {
        val currentPath = paths[selectedTab] ?: return this
        return copy(
            paths = paths + (selectedTab to currentPath.copy(
                destinations = currentPath.destinations + destination
            ))
        )
    }

    private fun NavigationState.pop(): NavigationState {
        val currentPath = paths[selectedTab] ?: return this
        if (!currentPath.canGoBack) return this
        return copy(
            paths = paths + (selectedTab to currentPath.copy(
                destinations = currentPath.destinations.dropLast(1)
            ))
        )
    }

    private fun NavigationState.pullToRoot(
        tab: com.taskbridge.core.models.navigation.AppTab
    ): NavigationState {
        val path = paths[tab] ?: return this
        val root = path.root ?: return this
        return copy(
            paths = paths + (tab to path.copy(destinations = listOf(root)))
        )
    }
}
