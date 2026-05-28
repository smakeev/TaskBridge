package com.taskbridge.core.models.navigation

public sealed interface NavigationDestinationMessage {
    public val scopeId: String

    public data class ElementId(
        val value: String,
        override val scopeId: String
    ) : NavigationDestinationMessage

    public data class TaskElement(
        val taskId: String,
        val parentPath: List<String>,
        override val scopeId: String
    ) : NavigationDestinationMessage
}
