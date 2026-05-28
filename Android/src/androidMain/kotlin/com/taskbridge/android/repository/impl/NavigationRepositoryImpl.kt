package com.taskbridge.android.repository.impl

import com.taskbridge.android.repository.NavigationRepository
import com.taskbridge.core.interactors.navigation.NavigationInteractor
import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.navigation.NavigationDestination
import com.taskbridge.core.models.navigation.NavigationDestinationMessage
import com.taskbridge.core.models.navigation.NavigationOverlay
import com.taskbridge.core.models.navigation.NavigationPath
import kotlinx.coroutines.flow.Flow

class NavigationRepositoryImpl(
    private val interactor: NavigationInteractor
) : NavigationRepository {
    override val activePath: Flow<NavigationPath?> = interactor.activePath
    override val currentTab: Flow<AppTab> = interactor.currentTab
    override val overlay: Flow<NavigationOverlay?> = interactor.overlay

    override suspend fun selectTab(tab: AppTab) {
        interactor.selectTab(tab)
    }

    override suspend fun pushDestination(destination: NavigationDestination) {
        interactor.pushDestination(destination)
    }

    override suspend fun popDestination() {
        interactor.popDestination()
    }

    override suspend fun pullToRoot(tab: AppTab) {
        interactor.pullToRoot(tab)
    }

    override suspend fun fetchActivePath(): NavigationPath? {
        return interactor.fetchActivePath()
    }

    override suspend fun fetchCurrentTab(): AppTab {
        return interactor.fetchCurrentTab()
    }

    override suspend fun setNavigationDestinationMessage(message: NavigationDestinationMessage?) {
        interactor.setNavigationDestinationMessage(message)
    }

    override suspend fun consumeNavigationDestinationMessage(scopeId: String): NavigationDestinationMessage? {
        return interactor.consumeNavigationDestinationMessage(scopeId)
    }
}
