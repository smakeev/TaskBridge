package com.taskbridge.core.composition

import com.taskbridge.core.stories.*
import com.taskbridge.core.stories.remote.*
import com.taskbridge.core.stories.tasks.*
import kotlin.reflect.KClass

/**
 * Container for internal user stories.
 * Provides explicit getters for stories.
 */
internal class UserStoriesContainer {
    fun selectTab(assembler: CoreAssembler): SelectTabStory = SelectTabStory(assembler)

    fun pushDestination(assembler: CoreAssembler): PushDestinationStory = PushDestinationStory(assembler)

    fun popDestination(assembler: CoreAssembler): PopDestinationStory = PopDestinationStory(assembler)
    
    fun getAppStateService(assembler: CoreAssembler): GetAppStateServiceStory = GetAppStateServiceStory(assembler)

    fun subscribeToNavigation(assembler: CoreAssembler): SubscribeToNavigationStory = SubscribeToNavigationStory(assembler)

    fun subscribeToActivePath(assembler: CoreAssembler): SubscribeToActivePathStory = SubscribeToActivePathStory(assembler)

    fun subscribeToOverlay(assembler: CoreAssembler): SubscribeToOverlayStory = SubscribeToOverlayStory(assembler)

    fun loadRemoteResource(assembler: CoreAssembler): LoadRemoteResourceStory = LoadRemoteResourceStory(assembler)

    fun forceLoadRemoteResource(assembler: CoreAssembler): ForceLoadRemoteResourceStory = ForceLoadRemoteResourceStory(assembler)

    fun observeRemoteResource(assembler: CoreAssembler): ObserveRemoteResourceStory = ObserveRemoteResourceStory(assembler)

    fun loadTasks(assembler: CoreAssembler): LoadTasksStory = LoadTasksStory(assembler)

    fun createTask(assembler: CoreAssembler): CreateTaskStory = CreateTaskStory(assembler)

    fun replaceTask(assembler: CoreAssembler): ReplaceTaskStory = ReplaceTaskStory(assembler)

    fun deleteTaskTree(assembler: CoreAssembler): DeleteTaskTreeStory = DeleteTaskTreeStory(assembler)

    fun observeTasks(assembler: CoreAssembler): ObserveTasksStory = ObserveTasksStory(assembler)
}
