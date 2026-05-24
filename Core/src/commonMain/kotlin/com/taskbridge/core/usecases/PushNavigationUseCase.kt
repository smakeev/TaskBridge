package com.taskbridge.core.usecases

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.navigation.NavigationDestination

/**
 * Use case for push navigation inside the selected tab stack.
 */
public class PushNavigationUseCase internal constructor(
    private val assembler: CoreAssembler
) {
    public suspend fun pushDestination(destination: NavigationDestination) {
        val story = assembler.stories.pushDestination(assembler)
        story(destination)
    }

    public suspend fun popDestination() {
        val story = assembler.stories.popDestination(assembler)
        story()
    }

    public suspend fun pullToRoot(tab: AppTab) {
        val story = assembler.stories.pullToRoot(assembler)
        story(tab)
    }
}
