package com.taskbridge.android.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

class ScrollBlinkHighlighter internal constructor() {
    var highlightedId by mutableStateOf<String?>(null)
        private set

    val alpha = Animatable(0f)
    private var activeId: String? = null

    suspend fun scrollToAndBlink(
        id: String,
        listState: LazyListState,
        presentationDelayMillis: Long = 0,
        findItemIndex: suspend () -> Int
    ) {
        if (activeId == id) return
        activeId = id
        try {
            var index = findItemIndex()
            var waitedMillis = 0
            while (index < 0 && waitedMillis < 2_000) {
                delay(50)
                waitedMillis += 50
                index = findItemIndex()
            }
            if (index < 0) return

            highlightedId = id
            alpha.snapTo(0f)
            listState.animateScrollToItem(index)
            if (presentationDelayMillis > 0) {
                delay(presentationDelayMillis)
            }

            repeat(2) {
                alpha.animateTo(1f, animationSpec = tween(durationMillis = 1_000))
                alpha.animateTo(0f, animationSpec = tween(durationMillis = 1_000))
            }
            if (highlightedId == id) {
                highlightedId = null
            }
        } finally {
            if (activeId == id) {
                activeId = null
            }
        }
    }
}

@Composable
fun rememberScrollBlinkHighlighter(): ScrollBlinkHighlighter {
    return remember { ScrollBlinkHighlighter() }
}
