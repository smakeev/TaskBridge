package com.taskbridge.core.models.navigation

public sealed interface NavigationOverlay {

    public data class Alert(
        val alertId: String?,
        val titleKey: String,
        val messageKey: String,
        val actions: List<OverlayAction>
    ) : NavigationOverlay

    public data class Sheet(
        val path: List<OverlayDestination>
    ) : NavigationOverlay

    public data class BottomOverlay(
        val heightPercent: Float,
        val path: List<OverlayDestination>
    ) : NavigationOverlay
}

public sealed interface OverlayDestination {

    public data class SelectTaskType(
        val parentTaskId: String?
    ) : OverlayDestination
}

public data class OverlayAction(
    val actionId: String,
    val titleKey: String,
    val style: OverlayActionStyle
)

public enum class OverlayActionStyle {
    DEFAULT,
    CANCEL,
    DESTRUCTIVE
}
