package com.taskbridge.core.composition

import com.taskbridge.core.network.HttpJsonClient
import com.taskbridge.core.network.JsonRequestManager
import com.taskbridge.core.services.appstate.AppStateService
import com.taskbridge.core.services.network.NetworkService
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

    private val httpJsonClientInstance: HttpJsonClient by lazy {
        HttpJsonClient()
    }

    private val jsonRequestManagerInstance: JsonRequestManager by lazy {
        JsonRequestManager(httpJsonClientInstance, scope)
    }

    private val networkServiceInstance: NetworkService by lazy {
        NetworkService(jsonRequestManagerInstance, scope)
    }

    fun appStateService(): AppStateService = appStateServiceInstance

    fun networkService(): NetworkService = networkServiceInstance
}
