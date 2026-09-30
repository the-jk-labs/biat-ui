package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/** Keyboard axis of a Toolbar. Horizontal uses Left/Right, Vertical uses Up/Down. */
enum class ToolbarOrientation { Horizontal, Vertical }

/**
 * Logic-only state for a headless Toolbar. No UI, no styling.
 *
 * A toolbar is a roving-tabindex container: exactly one item is focusable at a
 * time ([focusedIndex]); arrows move it with wrap-around, Home/End jump.
 * Selection/activation stays caller-owned; this state only tracks focus order.
 * Disabled indices are skipped by arrow moves.
 */
@Stable
class ToolbarState(
    itemCount: Int,
    initialFocused: Int = 0,
    loop: Boolean = true,
    disabledIndices: Set<Int> = emptySet(),
) {
    /** Item count. Managed by [rememberToolbarState]; assign only to reflect the caller's value. */
    private var itemCountState = mutableIntStateOf(itemCount)
    var itemCount: Int
        get() = itemCountState.intValue
        set(value) {
            itemCountState.intValue = value
            if (value == 0) {
                // Nothing focusable; keep index stable until items return.
            } else if (focusedIndex >= value) {
                focus(value - 1)
            }
        }

    /** Wrap-around. Managed by [rememberToolbarState]; assign only to reflect the caller's value. */
    var loop: Boolean by mutableStateOf(loop)
    var focusedIndex: Int by mutableIntStateOf(
        initialFocused.coerceIn(0, (itemCount - 1).coerceAtLeast(0)),
    )
        private set

    var disabledIndices: Set<Int> by mutableStateOf(disabledIndices)

    fun isEnabled(index: Int): Boolean = index !in disabledIndices

    fun focus(index: Int) {
        if (itemCount == 0) return
        val clamped = index.coerceIn(0, itemCount - 1)
        focusedIndex = snapToEnabled(clamped, 0) ?: clamped
    }

    fun move(delta: Int) {
        if (itemCount == 0) return
        val next = resolveToolbarIndex(focusedIndex, itemCount, delta, loop, disabledIndices)
        if (next != -1) focusedIndex = next
    }

    fun moveToFirst() {
        if (itemCount == 0) return
        val first = (0 until itemCount).firstOrNull { isEnabled(it) } ?: return
        focusedIndex = first
    }

    fun moveToLast() {
        if (itemCount == 0) return
        val last = (itemCount - 1 downTo 0).firstOrNull { isEnabled(it) } ?: return
        focusedIndex = last
    }

    private fun snapToEnabled(
        index: Int,
        delta: Int,
    ): Int? {
        if (isEnabled(index)) return index
        return resolveToolbarIndex(index, itemCount, if (delta == 0) 1 else delta, loop, disabledIndices)
            .takeIf { it != -1 }
    }
}

/**
 * Remembers [ToolbarState] across recompositions. The instance survives
 * [itemCount] changes; counts and [disabledIndices] are reflected every
 * recomposition with focus clamped to an enabled item.
 */
@Composable
fun rememberToolbarState(
    itemCount: Int,
    initialFocused: Int = 0,
    loop: Boolean = true,
    disabledIndices: Set<Int> = emptySet(),
): ToolbarState =
    remember {
        ToolbarState(itemCount = itemCount, initialFocused = initialFocused, loop = loop)
    }.apply {
        this.itemCount = itemCount
        this.loop = loop
        this.disabledIndices = disabledIndices
    }

/**
 * Pure roving-index math for toolbars. Moves [delta] steps from [current],
 * skipping [disabled]; wraps when [loop] is true. Returns -1 when every
 * item is disabled. Unit-tested.
 */
fun resolveToolbarIndex(
    current: Int,
    itemCount: Int,
    delta: Int,
    loop: Boolean = true,
    disabled: Set<Int> = emptySet(),
): Int {
    if (itemCount <= 0) return -1
    if (disabled.size >= itemCount) return -1
    val step =
        if (delta == 0) {
            1
        } else if (delta > 0) {
            1
        } else {
            -1
        }
    var index = current
    repeat(itemCount) {
        index =
            if (loop) {
                (index + step).mod(itemCount)
            } else {
                (index + step).coerceIn(0, itemCount - 1)
            }
        if (index !in disabled) return index
        if (!loop && (index == 0 || index == itemCount - 1) && index in disabled) {
            // Clamped at the edge; keep scanning inward is impossible, stop.
        }
    }
    return -1
}
