package com.taskbridge.core.models.navigation

data class NavigationPath(
    val destinations: List<NavigationDestination>
) {
    val root: NavigationDestination?
        get() = destinations.firstOrNull()

    val current: NavigationDestination?
        get() = destinations.lastOrNull()

    val canGoBack: Boolean
        get() = destinations.size > 1
}
