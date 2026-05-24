package com.taskbridge.core.interactors.navigation

import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.navigation.NavigationDestination
import com.taskbridge.core.models.navigation.NavigationOverlay
import com.taskbridge.core.models.navigation.NavigationPath
import com.taskbridge.core.usecases.NavigationStateObserverUseCase
import com.taskbridge.core.usecases.PushNavigationUseCase
import com.taskbridge.core.usecases.SelectTabUseCase
import kotlinx.coroutines.flow.Flow

/**
 * Platform-facing interactor for navigation.
 * Uses specific use cases provided by the CoreAssembler.
 */
public class NavigationInteractor(
    private val selectTabUseCase: SelectTabUseCase,
    private val pushNavigationUseCase: PushNavigationUseCase,
    private val observerUseCase: NavigationStateObserverUseCase
) {
    public val activePath: Flow<NavigationPath?> = observerUseCase.subscribeOnActivePath()
    public val currentTab: Flow<AppTab> = observerUseCase.subscribeOnCurrentTab()
    public val overlay: Flow<NavigationOverlay?> = observerUseCase.subscribeOnOverlay()

    public suspend fun selectTab(tab: AppTab) {
        selectTabUseCase.selectTab(tab)
    }

    public suspend fun pushDestination(destination: NavigationDestination) {
        pushNavigationUseCase.pushDestination(destination)
    }

    public suspend fun popDestination() {
        pushNavigationUseCase.popDestination()
    }

    public suspend fun pullToRoot(tab: AppTab) {
        pushNavigationUseCase.pullToRoot(tab)
    }

    public suspend fun fetchActivePath(): NavigationPath? {
        return observerUseCase.fetchActivePath()
    }

    public suspend fun fetchCurrentTab(): AppTab {
        return observerUseCase.fetchCurrentTab()
    }

    public suspend fun fetchOverlay(): NavigationOverlay? {
        return observerUseCase.fetchOverlay()
    }
}
