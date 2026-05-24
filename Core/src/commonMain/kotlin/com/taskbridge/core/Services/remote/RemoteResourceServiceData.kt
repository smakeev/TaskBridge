package com.taskbridge.core.services.remote

import com.taskbridge.core.services.common.ServiceData

internal data class RemoteResourceServiceData(
    val resources: Map<String, RemoteResourceEntry> = emptyMap()
) : ServiceData {
    fun resource(url: String): RemoteResourceEntry? = resources[url]
}
