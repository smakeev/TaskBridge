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
public class NavigationStateObserverUseCase internal constructor(
    private val assembler: CoreAssembler
) {
    public fun subscribeOnActivePath(): Flow<NavigationPath?> {
        return assembler.stories.subscribeToActivePath(assembler)()
    }

    public fun subscribeOnCurrentTab(): Flow<AppTab> {
        return assembler.stories.subscribeToNavigation(assembler)()
            .map { it.selectedTab }
    }

    public fun subscribeOnOverlay(): Flow<NavigationOverlay?> {
        return assembler.stories.subscribeToOverlay(assembler)()
    }

    public suspend fun fetchActivePath(): NavigationPath? {
        return subscribeOnActivePath().first()
    }

    public suspend fun fetchCurrentTab(): AppTab {
        return subscribeOnCurrentTab().first()
    }

    public suspend fun fetchOverlay(): NavigationOverlay? {
        return subscribeOnOverlay().first()
    }
}
