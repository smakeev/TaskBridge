package com.taskbridge.core.interactors.navigation

import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.navigation.NavigationPath
import com.taskbridge.core.usecases.NavigationStateObserverUseCase
import com.taskbridge.core.usecases.SelectTabUseCase
import kotlinx.coroutines.flow.Flow

/**
 * Platform-facing interactor for navigation.
 * Uses specific use cases provided by the CoreAssembler.
 */
public class NavigationInteractor(
    private val selectTabUseCase: SelectTabUseCase,
    private val observerUseCase: NavigationStateObserverUseCase
) {
    public val activePath: Flow<NavigationPath?> = observerUseCase.subscribeOnActivePath()
    public val currentTab: Flow<AppTab> = observerUseCase.subscribeOnCurrentTab()

    public suspend fun selectTab(tab: AppTab) {
        selectTabUseCase.selectTab(tab)
    }

    public suspend fun fetchActivePath(): NavigationPath? {
        return observerUseCase.fetchActivePath()
    }

    public suspend fun fetchCurrentTab(): AppTab {
        return observerUseCase.fetchCurrentTab()
    }
}
