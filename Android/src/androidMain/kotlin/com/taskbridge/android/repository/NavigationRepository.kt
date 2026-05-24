package com.taskbridge.android.repository

import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.navigation.NavigationOverlay
import com.taskbridge.core.models.navigation.NavigationPath
import kotlinx.coroutines.flow.Flow

interface NavigationRepository {
    val activePath: Flow<NavigationPath?>
    val currentTab: Flow<AppTab>
    val overlay: Flow<NavigationOverlay?>

    suspend fun selectTab(tab: AppTab)
}
