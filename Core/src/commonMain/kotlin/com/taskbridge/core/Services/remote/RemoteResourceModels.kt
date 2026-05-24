package com.taskbridge.core.services.remote

internal enum class RemoteResourceStatus {
    IDLE,
    LOADING,
    LOADED,
    FAILED
}

internal data class RemoteResourceEntry(
    val url: String,
    val status: RemoteResourceStatus,
    val data: Any?,
    val errorMessage: String?,
    val loadedAtMillis: Long?,
    val ttlMillis: Long,
    val autoRefreshEnabled: Boolean
)
