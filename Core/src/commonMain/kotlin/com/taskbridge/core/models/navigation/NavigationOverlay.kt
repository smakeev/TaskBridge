package com.taskbridge.core.models.navigation

sealed interface NavigationOverlay {

    data class Alert(
        val alertId: String?,
        val titleKey: String,
        val messageKey: String,
        val actions: List<OverlayAction>
    ) : NavigationOverlay

    data class Sheet(
        val path: List<OverlayDestination>
    ) : NavigationOverlay

    data class BottomOverlay(
        val heightPercent: Float,
        val path: List<OverlayDestination>
    ) : NavigationOverlay
}

sealed interface OverlayDestination {

    data class SelectTaskType(
        val parentTaskId: String?
    ) : OverlayDestination
}

data class OverlayAction(
    val actionId: String,
    val titleKey: String,
    val style: OverlayActionStyle
)

enum class OverlayActionStyle {
    DEFAULT,
    CANCEL,
    DESTRUCTIVE
}
