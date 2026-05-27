package com.taskbridge.core.models.messages

public sealed class AppMessageError(
    message: String
) : Exception(message) {
    public data class UnsupportedCoreMessage(
        val typeName: String
    ) : AppMessageError("Unsupported core message: $typeName")
}
