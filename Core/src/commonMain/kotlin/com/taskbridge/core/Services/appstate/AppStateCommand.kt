package com.taskbridge.core.services.appstate

import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.navigation.NavigationDestination
import com.taskbridge.core.models.navigation.NavigationDestinationMessage
import com.taskbridge.core.services.common.ServiceCommand
import kotlinx.coroutines.CompletableDeferred

internal sealed interface AppStateCommand : ServiceCommand {
    data class SelectTab(
        val tab: AppTab
    ) : AppStateCommand

    data class PushDestination(
        val destination: NavigationDestination
    ) : AppStateCommand

    data object PopDestination : AppStateCommand

    data class PullToRoot(
        val tab: AppTab
    ) : AppStateCommand

    data class SetNavigationDestinationMessage(
        val message: NavigationDestinationMessage?
    ) : AppStateCommand

    /**
     * Atomically consumes the pending navigation message for [scopeId]: if the current
     * message belongs to that scope it is returned (via [result]) and cleared in the same
     * reducer step; otherwise [result] completes with null and the message is left intact.
     * This keeps read-check-clear inside the actor (see Core-3) instead of splitting it
     * across a bypassing read and a separately queued clear.
     */
    data class ConsumeNavigationDestinationMessage(
        val scopeId: String,
        val result: CompletableDeferred<NavigationDestinationMessage?>
    ) : AppStateCommand
}
