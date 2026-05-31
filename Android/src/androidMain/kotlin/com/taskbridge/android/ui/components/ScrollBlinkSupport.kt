package com.taskbridge.android.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.taskbridge.core.models.messages.AppMessage
import com.taskbridge.core.models.navigation.NavigationDestinationMessage
import kotlinx.coroutines.flow.Flow

/**
 * Returns a stable `suspend (id) -> Unit` that scrolls the given list to the row
 * for `id` and blinks it via [ScrollBlinkHighlighter]. The returned lambda is safe
 * to capture in a `LaunchedEffect`: it is remembered across recompositions, while
 * [findItemIndex] is read through [rememberUpdatedState] so it always sees the
 * latest list contents. Shared by the Tasks root/details and Reminders screens
 * (see Audit Common-3 / And-7).
 */
@Composable
fun rememberScrollToAndBlink(
    highlighter: ScrollBlinkHighlighter,
    listState: LazyListState,
    findItemIndex: (id: String) -> Int
): suspend (String) -> Unit {
    val currentFindItemIndex by rememberUpdatedState(findItemIndex)
    return remember(highlighter, listState) {
        { id: String ->
            highlighter.scrollToAndBlink(
                id = id,
                listState = listState,
                findItemIndex = { currentFindItemIndex(id) }
            )
        }
    }
}

/**
 * The scroll-and-blink wiring shared by every blinking list (Tasks root/details,
 * Reminders, and any future screen). Domain-neutral: two highlight sources reduced to
 * *the id of the row to blink* — a live stream of "created" app messages (blink in
 * place) and a pending navigation-destination message consumed on appear. See Audit
 * Common-3.
 *
 * @param createdMessages factory for the live created-message stream (kept stable via
 *   [scopeId]-keyed effects, so it is created once per scope).
 * @param createdTargetId maps a created message to the row id to blink, or null to skip
 *   (also where a screen filters to its own scope).
 * @param consumePending consumes + clears the pending message for [scopeId].
 * @param pendingTargetId maps a consumed message to the row id to blink, or null.
 * @param findItemIndex data-row id → `LazyColumn` index (with header offset), or -1.
 */
@Composable
fun ScrollBlinkEffects(
    highlighter: ScrollBlinkHighlighter,
    listState: LazyListState,
    scopeId: String,
    createdMessages: () -> Flow<AppMessage>,
    createdTargetId: (AppMessage) -> String?,
    consumePending: suspend (scopeId: String) -> NavigationDestinationMessage?,
    pendingTargetId: (NavigationDestinationMessage) -> String?,
    findItemIndex: (id: String) -> Int
) {
    val scrollToAndBlink = rememberScrollToAndBlink(highlighter, listState, findItemIndex)
    val currentCreatedMessages by rememberUpdatedState(createdMessages)
    val currentCreatedTargetId by rememberUpdatedState(createdTargetId)
    val currentConsumePending by rememberUpdatedState(consumePending)
    val currentPendingTargetId by rememberUpdatedState(pendingTargetId)

    LaunchedEffect(scopeId) {
        currentCreatedMessages().collect { message ->
            val id = currentCreatedTargetId(message) ?: return@collect
            scrollToAndBlink(id)
        }
    }

    LaunchedEffect(scopeId) {
        val message = currentConsumePending(scopeId) ?: return@LaunchedEffect
        val id = currentPendingTargetId(message) ?: return@LaunchedEffect
        scrollToAndBlink(id)
    }
}

/**
 * Number of leading `item {}`s a `LazyColumn` renders before its data rows, used to
 * convert a data index into a list index for `animateScrollToItem`. There is always
 * a header (1); the loading / error / empty placeholders each add one when shown.
 */
fun leadingItemsOffset(
    showLoading: Boolean,
    showError: Boolean,
    showEmpty: Boolean
): Int {
    var offset = 1
    if (showLoading) offset += 1
    if (showError) offset += 1
    if (showEmpty) offset += 1
    return offset
}
