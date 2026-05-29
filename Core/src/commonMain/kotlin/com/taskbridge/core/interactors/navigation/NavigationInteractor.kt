package com.taskbridge.core.interactors.navigation

import com.taskbridge.core.composition.CoreAccess
import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.navigation.NavigationDestination
import com.taskbridge.core.models.navigation.NavigationDestinationMessage
import com.taskbridge.core.models.navigation.NavigationOverlay
import com.taskbridge.core.models.navigation.NavigationPath
import kotlinx.coroutines.flow.Flow

/**
 * Platform-facing interactor for navigation.
 *
 * The [accessToken] parameter prevents construction outside the Core module:
 * only Core can mint a [CoreAccess] instance. Internal dependencies are
 * pulled from the token; their types are not exposed in this signature.
 */
public class NavigationInteractor public constructor(
    accessToken: CoreAccess
) {
    private val selectTabUseCase = accessToken.dependencies.selectTabUseCase()
    private val pushNavigationUseCase = accessToken.dependencies.pushNavigationUseCase()
    private val observerUseCase = accessToken.dependencies.navigationObserverUseCase()
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

    public suspend fun setNavigationDestinationMessage(message: NavigationDestinationMessage?) {
        pushNavigationUseCase.setNavigationDestinationMessage(message)
    }

    public suspend fun consumeNavigationDestinationMessage(
        scopeId: String
    ): NavigationDestinationMessage? {
        return pushNavigationUseCase.consumeNavigationDestinationMessage(scopeId)
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
