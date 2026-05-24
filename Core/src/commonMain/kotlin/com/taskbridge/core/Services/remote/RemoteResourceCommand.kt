package com.taskbridge.core.services.remote

import com.taskbridge.core.services.common.ServiceCommand

internal sealed interface RemoteResourceCommand : ServiceCommand {
    val url: String
    val ttlMillis: Long
    val autoRefreshEnabled: Boolean
    val loader: suspend () -> Any?

    data class Load(
        override val url: String,
        override val ttlMillis: Long,
        override val autoRefreshEnabled: Boolean,
        override val loader: suspend () -> Any?
    ) : RemoteResourceCommand

    data class ForceLoad(
        override val url: String,
        override val ttlMillis: Long,
        override val autoRefreshEnabled: Boolean,
        override val loader: suspend () -> Any?
    ) : RemoteResourceCommand
}
