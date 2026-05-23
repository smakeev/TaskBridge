package com.taskbridge.core.interactors.navigation

import com.taskbridge.core.TaskBridge
import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.navigation.NavigationPath
import com.taskbridge.core.usecases.NavigationStateObserverUseCase
import com.taskbridge.core.usecases.SelectTabUseCase
import kotlinx.coroutines.flow.Flow

/**
 * Platform-facing interactor for navigation.
 * Simplifies access to navigation use cases for Android and iOS.
 */
public class NavigationInteractor(
    private val taskBridge: TaskBridge
) {
    private val selectTabUseCase = taskBridge.getUseCase(SelectTabUseCase::class)
    private val observerUseCase = taskBridge.getUseCase(NavigationStateObserverUseCase::class)

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
