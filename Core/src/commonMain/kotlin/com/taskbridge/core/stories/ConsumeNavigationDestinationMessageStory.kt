package com.taskbridge.core.stories

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.navigation.NavigationDestinationMessage
import com.taskbridge.core.services.appstate.AppStateCommand
import kotlinx.coroutines.CompletableDeferred

/**
 * Atomically consumes the pending navigation message for [scopeId]: returns it (and
 * clears it inside the actor) if it belongs to this scope, otherwise returns null and
 * leaves it untouched. Replaces the old fetch-then-set pair so read-check-clear happens
 * as a single command (see Core-3).
 */
internal class ConsumeNavigationDestinationMessageStory(
    private val assembler: CoreAssembler
) {
    suspend operator fun invoke(scopeId: String): NavigationDestinationMessage? {
        val appStateService = assembler.services.appStateService()
        val result = CompletableDeferred<NavigationDestinationMessage?>()
        appStateService.sendCommand(
            AppStateCommand.ConsumeNavigationDestinationMessage(scopeId, result)
        )
        return result.await()
    }
}
