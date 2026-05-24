package com.taskbridge.core.usecases

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.navigation.NavigationDestination

/**
 * Use case for selecting a tab.
 */
public class SelectTabUseCase internal constructor(
    private val assembler: CoreAssembler
) {
    public suspend fun selectTab(tab: AppTab) {
        val story = assembler.stories.selectTab(assembler)
        story(tab)
    }

    public suspend fun pushDestination(destination: NavigationDestination) {
        val story = assembler.stories.pushDestination(assembler)
        story(destination)
    }

    public suspend fun popDestination() {
        val story = assembler.stories.popDestination(assembler)
        story()
    }
}
