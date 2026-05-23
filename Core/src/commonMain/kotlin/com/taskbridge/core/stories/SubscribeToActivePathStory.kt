package com.taskbridge.core.stories

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.navigation.NavigationPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Story for subscribing to the active navigation path.
 */
internal class SubscribeToActivePathStory(
    private val assembler: CoreAssembler
) {
    operator fun invoke(): Flow<NavigationPath?> {
        val subscribeToNavigation = assembler.stories.subscribeToNavigation(assembler)
        return subscribeToNavigation()
            .map { it.activePath }
            .distinctUntilChanged()
    }
}
