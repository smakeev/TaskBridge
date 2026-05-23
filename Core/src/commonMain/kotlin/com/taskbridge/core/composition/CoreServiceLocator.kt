package com.taskbridge.core.composition

import com.taskbridge.core.services.appstate.AppStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Locator for internal services.
 * Creates and keeps service instances lazily.
 */
internal class CoreServiceLocator {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val appStateServiceInstance: AppStateService by lazy {
        AppStateService(scope)
    }

    fun appStateService(): AppStateService = appStateServiceInstance
}
