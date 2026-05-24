package com.taskbridge.core.services.appstate

import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.services.common.ServiceCommand

internal sealed interface AppStateCommand : ServiceCommand {
    data class SelectTab(
        val tab: AppTab
    ) : AppStateCommand
}
