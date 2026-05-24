package com.taskbridge.android.repository

import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.navigation.NavigationDestination
import com.taskbridge.core.models.navigation.NavigationOverlay
import com.taskbridge.core.models.navigation.NavigationPath
import kotlinx.coroutines.flow.Flow

interface NavigationRepository {
    val activePath: Flow<NavigationPath?>
    val currentTab: Flow<AppTab>
    val overlay: Flow<NavigationOverlay?>

    suspend fun selectTab(tab: AppTab)
    suspend fun pushDestination(destination: NavigationDestination)
    suspend fun popDestination()
    suspend fun pullToRoot(tab: AppTab)
}
