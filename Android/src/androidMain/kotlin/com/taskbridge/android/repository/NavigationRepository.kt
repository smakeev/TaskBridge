package com.taskbridge.android.repository

import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.navigation.NavigationDestination
import com.taskbridge.core.models.navigation.NavigationDestinationMessage
import com.taskbridge.core.models.navigation.NavigationOverlay
import com.taskbridge.core.models.navigation.NavigationPath
import kotlinx.coroutines.flow.Flow

sealed interface NavigationDestinationMessageScopeId {
    val scopeId: String

    data object TasksRoot : NavigationDestinationMessageScopeId {
        override val scopeId: String = "tasksRoot"
    }

    data class TaskDetails(
        val parentId: String
    ) : NavigationDestinationMessageScopeId {
        override val scopeId: String = "taskDetails:$parentId"
    }

    data object RemindersRoot : NavigationDestinationMessageScopeId {
        override val scopeId: String = "remindersRoot"
    }
}

interface NavigationRepository {
    val activePath: Flow<NavigationPath?>
    val currentTab: Flow<AppTab>
    val overlay: Flow<NavigationOverlay?>

    suspend fun selectTab(tab: AppTab)
    suspend fun pushDestination(destination: NavigationDestination)
    suspend fun popDestination()
    suspend fun pullToRoot(tab: AppTab)
    suspend fun fetchActivePath(): NavigationPath?
    suspend fun fetchCurrentTab(): AppTab
    suspend fun setNavigationDestinationMessage(message: NavigationDestinationMessage?)
    suspend fun consumeNavigationDestinationMessage(scopeId: String): NavigationDestinationMessage?
}
