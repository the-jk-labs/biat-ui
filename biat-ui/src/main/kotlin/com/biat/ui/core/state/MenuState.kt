package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Logic-only state for menus (dropdown / context).
 * Tracks open state + highlighted index for keyboard navigation.
 *
 * Openness contract (controlled vs uncontrolled):
 * - Uncontrolled (default): pass [initialOpen]; the state owns the value and
 *   [open]/[close]/[toggle]/[setOpen] mutate it, notifying [onOpenChange].
 * - Controlled: pass non-null [controlledOpen] plus [onOpenChange]; the caller
 *   owns the value and [open]/[close]/[toggle]/[setOpen] only notify via
 *   [onOpenChange] without mutating. The caller must reflect the new value
 *   back into [controlledOpen] ([rememberMenuState] does this every
 *   recomposition). Do not switch modes during the state's lifetime.
 * - Highlight is transient internal state in both modes: [close] always
 *   resets it, since dismiss intent is explicit either way.
 *
 * Items contract: [itemCount] counts highlightable entries (plain items,
 * checkbox/radio items, submenu triggers). Separators are caller-drawn and
 * excluded. Indices in [disabledIndices] are skipped by [moveHighlight] and
 * snapped past by [highlight]; when every item is disabled highlight stays
 * -1. [MenuItem] with enabled=false must sit at a disabled index.
 */
@Stable
class MenuState(
    initialOpen: Boolean = false,
    itemCount: Int = 0,
    disabledIndices: Set<Int> = emptySet(),
    controlledOpen: Boolean? = null,
    var onOpenChange: ((Boolean) -> Unit)? = null,
) {
    private var internalOpen by mutableStateOf(initialOpen)

    /**
     * Caller-owned value in controlled mode. Managed by [rememberMenuState];
     * assign only to reflect the caller's value.
     */
    var controlledOpen: Boolean? by mutableStateOf(controlledOpen)

    /** Highlightable entry count. Managed by [rememberMenuState]; assign only to reflect the caller's value. */
    private var itemCountState = mutableIntStateOf(itemCount)
    var itemCount: Int
        get() = itemCountState.intValue
        set(value) {
            itemCountState.intValue = value
            if (highlightedIndex >= value) highlightedIndex = -1
        }

    /** Disabled indices. Managed by [rememberMenuState]; assign only to reflect the caller's value. */
    var disabledIndices: Set<Int> by mutableStateOf(disabledIndices)

    val isOpen: Boolean get() = controlledOpen ?: internalOpen

    /** Index highlighted for keyboard / hover. -1 = none. */
    var highlightedIndex by mutableIntStateOf(-1)
        internal set

    fun open() = setOpen(true)

    fun close() {
        highlightedIndex = -1
        setOpen(false)
    }

    fun toggle() {
        if (isOpen) close() else open()
    }

    @JvmName("setOpenState")
    fun setOpen(open: Boolean) {
        if (isOpen == open) return
        if (controlledOpen != null) {
            onOpenChange?.invoke(open)
        } else {
            internalOpen = open
            onOpenChange?.invoke(open)
        }
    }

    fun highlight(index: Int) {
        if (itemCount > 0) {
            highlightedIndex = snapToEnabled(index.coerceIn(-1, itemCount - 1))
        } else {
            highlightedIndex = index
        }
    }

    fun moveHighlight(delta: Int) {
        if (itemCount <= 0) {
            highlightedIndex += delta
            return
        }
        if (disabledIndices.size >= itemCount) {
            highlightedIndex = -1
            return
        }
        var next = highlightedIndex
        repeat(itemCount) {
            next =
                if (next < 0) {
                    if (delta > 0) 0 else itemCount - 1
                } else {
                    (next + delta).mod(itemCount)
                }
            if (isEnabled(next)) {
                highlightedIndex = next
                return
            }
        }
        highlightedIndex = -1
    }

    /** True when [index] participates in keyboard highlight. */
    fun isEnabled(index: Int): Boolean = index !in disabledIndices

    private fun snapToEnabled(index: Int): Int {
        if (index < 0 || isEnabled(index)) return index
        for (step in 1 until itemCount) {
            val candidate = (index + step) % itemCount
            if (isEnabled(candidate)) return candidate
        }
        return -1
    }
}

/**
 * Remembers [MenuState] across recompositions. The instance survives
 * [itemCount] changes (openness is kept, highlight clamps); [controlledOpen]
 * and [onOpenChange] are refreshed every recomposition. In controlled mode
 * open/close/toggle only notify.
 */
@Composable
fun rememberMenuState(
    initialOpen: Boolean = false,
    itemCount: Int = 0,
    disabledIndices: Set<Int> = emptySet(),
    controlledOpen: Boolean? = null,
    onOpenChange: ((Boolean) -> Unit)? = null,
): MenuState =
    remember {
        MenuState(
            initialOpen = initialOpen,
            itemCount = itemCount,
            disabledIndices = disabledIndices,
        )
    }.apply {
        this.itemCount = itemCount
        this.disabledIndices = disabledIndices
        this.controlledOpen = controlledOpen
        this.onOpenChange = onOpenChange
    }
