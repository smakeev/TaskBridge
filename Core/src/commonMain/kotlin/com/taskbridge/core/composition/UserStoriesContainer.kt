package com.taskbridge.core.composition

import com.taskbridge.core.stories.*
import kotlin.reflect.KClass

/**
 * Container for internal user stories.
 * Provides explicit getters for stories.
 */
internal class UserStoriesContainer {
    fun selectTab(assembler: CoreAssembler): SelectTabStory = SelectTabStory(assembler)
    
    fun getAppStateService(assembler: CoreAssembler): GetAppStateServiceStory = GetAppStateServiceStory(assembler)

    fun subscribeToNavigation(assembler: CoreAssembler): SubscribeToNavigationStory = SubscribeToNavigationStory(assembler)

    fun subscribeToActivePath(assembler: CoreAssembler): SubscribeToActivePathStory = SubscribeToActivePathStory(assembler)

    fun subscribeToOverlay(assembler: CoreAssembler): SubscribeToOverlayStory = SubscribeToOverlayStory(assembler)
}
