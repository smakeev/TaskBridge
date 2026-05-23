package com.taskbridge.core.composition

import com.taskbridge.core.stories.GetAppStateServiceStory
import com.taskbridge.core.stories.SelectTabStory

/**
 * Container for internal user stories.
 * Provides explicit getters for stories.
 */
internal class UserStoriesContainer {
    fun selectTab(assembler: CoreAssembler): SelectTabStory = SelectTabStory(assembler)
    
    fun getAppStateService(assembler: CoreAssembler): GetAppStateServiceStory = GetAppStateServiceStory(assembler)
}
