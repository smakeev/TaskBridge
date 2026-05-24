package com.taskbridge.core.services.appstate

import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.navigation.NavigationDestination
import com.taskbridge.core.models.navigation.NavigationPath
import com.taskbridge.core.models.navigation.NavigationState
import com.taskbridge.core.services.common.ServiceData

internal data class AppStateServiceData(
    val navigationState: NavigationState = initialNavigationState()
) : ServiceData

private fun initialNavigationState(): NavigationState {
    return NavigationState(
        selectedTab = AppTab.TASKS,
        paths = mapOf(
            AppTab.TASKS to NavigationPath(
                destinations = listOf(NavigationDestination.TasksRoot)
            ),
            AppTab.TEMPLATES to NavigationPath(
                destinations = listOf(NavigationDestination.TemplatesRoot)
            ),
            AppTab.REMINDERS to NavigationPath(
                destinations = listOf(NavigationDestination.RemindersRoot)
            )
        ),
        overlay = null
    )
}
