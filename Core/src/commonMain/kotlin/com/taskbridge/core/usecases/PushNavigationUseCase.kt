package com.taskbridge.core.usecases

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.navigation.NavigationDestination
import com.taskbridge.core.models.navigation.NavigationDestinationMessage

/**
 * Use case for push navigation inside the selected tab stack.
 */
internal class PushNavigationUseCase(
    private val assembler: CoreAssembler
) {
    suspend fun pushDestination(destination: NavigationDestination) {
        val story = assembler.stories.pushDestination(assembler)
        story(destination)
    }

    suspend fun popDestination() {
        val story = assembler.stories.popDestination(assembler)
        story()
    }

    suspend fun pullToRoot(tab: AppTab) {
        val story = assembler.stories.pullToRoot(assembler)
        story(tab)
    }

    suspend fun setNavigationDestinationMessage(message: NavigationDestinationMessage?) {
        val story = assembler.stories.setNavigationDestinationMessage(assembler)
        story(message)
    }

    suspend fun consumeNavigationDestinationMessage(
        scopeId: String
    ): NavigationDestinationMessage? {
        // Atomic read-check-clear inside AppStateService (see Core-3).
        val story = assembler.stories.consumeNavigationDestinationMessage(assembler)
        return story(scopeId)
    }
}
