package com.taskbridge.core.stories

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.navigation.NavigationOverlay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Story for subscribing to navigation overlays.
 */
internal class SubscribeToOverlayStory(
    private val assembler: CoreAssembler
) {
    operator fun invoke(): Flow<NavigationOverlay?> {
        val appStateService = assembler.services.appStateService()
        return appStateService.data
            .map { it.navigationState.overlay }
            .distinctUntilChanged()
    }
}
