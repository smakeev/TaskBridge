package com.taskbridge.core.stories

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.navigation.NavigationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Story for subscribing to the application's navigation state.
 */
internal class SubscribeToNavigationStory(
    private val assembler: CoreAssembler
) {
    operator fun invoke(): Flow<NavigationState> {
        val appStateService = assembler.stories.getAppStateService(assembler)()
        return appStateService.data
            .map { it.navigationState }
            .distinctUntilChanged()
    }
}
