package com.taskbridge.core.usecases

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.navigation.NavigationDestination
import com.taskbridge.core.models.navigation.NavigationDestinationMessage

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

    public suspend fun setNavigationDestinationMessage(message: NavigationDestinationMessage?) {
        val story = assembler.stories.setNavigationDestinationMessage(assembler)
        story(message)
    }

    public suspend fun consumeNavigationDestinationMessage(
        scopeId: String
    ): NavigationDestinationMessage? {
        val fetchStory = assembler.stories.fetchNavigationDestinationMessage(assembler)
        val state = fetchStory()
        val message = state.navigationDestinationMessage ?: return null
        if (message.scopeId != scopeId) return null

        // reset navigation message is current message is for us.
        val setStory = assembler.stories.setNavigationDestinationMessage(assembler)
        setStory(null)
        return message
    }
}
