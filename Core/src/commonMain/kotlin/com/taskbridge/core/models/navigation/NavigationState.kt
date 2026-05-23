package com.taskbridge.core.models.navigation

data class NavigationState(
    val selectedTab: AppTab,
    val paths: Map<AppTab, NavigationPath>,
    val overlay: NavigationOverlay?
) {
    val activePath: NavigationPath?
        get() = paths[selectedTab]
}
