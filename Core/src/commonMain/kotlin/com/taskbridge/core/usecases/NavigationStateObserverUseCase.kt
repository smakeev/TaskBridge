package com.taskbridge.core.usecases

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.navigation.NavigationOverlay
import com.taskbridge.core.models.navigation.NavigationPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Use case for observing and fetching navigation state.
 */
internal class NavigationStateObserverUseCase(
    private val assembler: CoreAssembler
) {
    fun subscribeOnActivePath(): Flow<NavigationPath?> {
        return assembler.stories.subscribeToActivePath(assembler)()
    }

    fun subscribeOnCurrentTab(): Flow<AppTab> {
        return assembler.stories.subscribeToNavigation(assembler)()
            .map { it.selectedTab }
    }

    fun subscribeOnOverlay(): Flow<NavigationOverlay?> {
        return assembler.stories.subscribeToOverlay(assembler)()
    }

    suspend fun fetchActivePath(): NavigationPath? {
        return subscribeOnActivePath().first()
    }

    suspend fun fetchCurrentTab(): AppTab {
        return subscribeOnCurrentTab().first()
    }

    suspend fun fetchOverlay(): NavigationOverlay? {
        return subscribeOnOverlay().first()
    }
}
