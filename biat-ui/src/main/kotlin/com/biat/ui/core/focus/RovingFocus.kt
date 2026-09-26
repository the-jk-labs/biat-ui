package com.biat.ui.core.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Roving focus state for Menu / Tabs / Select lists.
 * Only one item is "current"; arrows move it, Home/End jump.
 */
@Stable
class RovingFocusState(
    val itemCount: Int,
    initialIndex: Int = 0,
    val loop: Boolean = true,
    val orientation: Orientation = Orientation.Vertical,
) {
    var currentIndex by mutableIntStateOf(initialIndex.coerceIn(0, (itemCount - 1).coerceAtLeast(0)))
        private set

    val requesters: List<FocusRequester> = List(itemCount) { FocusRequester() }

    fun move(delta: Int) {
        if (itemCount == 0) return
        val next = currentIndex + delta
        currentIndex = if (loop) {
            next.mod(itemCount)
        } else {
            next.coerceIn(0, itemCount - 1)
        }
        requesters.getOrNull(currentIndex)?.requestFocus()
    }

    fun moveTo(index: Int) {
        if (itemCount == 0) return
        currentIndex = index.coerceIn(0, itemCount - 1)
        requesters.getOrNull(currentIndex)?.requestFocus()
    }

    fun focusCurrent() {
        requesters.getOrNull(currentIndex)?.requestFocus()
    }

    enum class Orientation { Vertical, Horizontal, Both }
}

@Composable
fun rememberRovingFocusState(
    itemCount: Int,
    initialIndex: Int = 0,
    loop: Boolean = true,
    orientation: RovingFocusState.Orientation = RovingFocusState.Orientation.Vertical,
): RovingFocusState = remember(itemCount, loop, orientation) {
    RovingFocusState(itemCount, initialIndex, loop, orientation)
}

/**
 * Key handling for a roving container. Attach to the container.
 * Returns true when the event was consumed.
 */
fun Modifier.rovingKeys(
    state: RovingFocusState,
    onSelectCurrent: (() -> Unit)? = null,
    onEscape: (() -> Unit)? = null,
): Modifier = this.onPreviewKeyEvent { event ->
    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
    val vertical = state.orientation != RovingFocusState.Orientation.Horizontal
    val horizontal = state.orientation != RovingFocusState.Orientation.Vertical
    when (event.key) {
        Key.DirectionDown -> if (vertical) { state.move(1); true } else false
        Key.DirectionUp -> if (vertical) { state.move(-1); true } else false
        Key.DirectionRight -> if (horizontal) { state.move(1); true } else false
        Key.DirectionLeft -> if (horizontal) { state.move(-1); true } else false
        Key.MoveHome -> { state.moveTo(0); true }
        Key.MoveEnd -> { state.moveTo(state.itemCount - 1); true }
        Key.Enter, Key.NumPadEnter -> { onSelectCurrent?.invoke(); onSelectCurrent != null }
        Key.Escape -> { onEscape?.invoke(); onEscape != null }
        else -> false
    }
}

/** Attach to item at [index] so roving state can request focus on it. */
fun Modifier.rovingItem(state: RovingFocusState, index: Int): Modifier {
    val requester = state.requesters.getOrNull(index) ?: return this
    return this.focusRequester(requester)
}
