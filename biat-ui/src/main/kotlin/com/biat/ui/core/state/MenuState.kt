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
 */
@Stable
class MenuState(
    initialOpen: Boolean = false,
    val itemCount: Int = 0,
    controlledOpen: Boolean? = null,
    var onOpenChange: ((Boolean) -> Unit)? = null,
) {
    private var internalOpen by mutableStateOf(initialOpen)

    /**
     * Caller-owned value in controlled mode. Managed by [rememberMenuState];
     * assign only to reflect the caller's value.
     */
    var controlledOpen: Boolean? by mutableStateOf(controlledOpen)

    val isOpen: Boolean get() = controlledOpen ?: internalOpen

    /** Index highlighted for keyboard / hover. -1 = none. */
    var highlightedIndex by mutableIntStateOf(-1)
        private set

    fun open() = setOpen(true)
    fun close() {
        highlightedIndex = -1
        setOpen(false)
    }
    fun toggle() = setOpen(!isOpen)

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
            highlightedIndex = index.coerceIn(-1, itemCount - 1)
        } else {
            highlightedIndex = index
        }
    }

    fun moveHighlight(delta: Int) {
        if (itemCount <= 0) {
            highlightedIndex += delta
            return
        }
        val next = if (highlightedIndex < 0) {
            if (delta > 0) 0 else itemCount - 1
        } else {
            (highlightedIndex + delta).mod(itemCount)
        }
        highlightedIndex = next
    }
}

@Composable
fun rememberMenuState(
    initialOpen: Boolean = false,
    itemCount: Int = 0,
    controlledOpen: Boolean? = null,
    onOpenChange: ((Boolean) -> Unit)? = null,
): MenuState = remember(itemCount) {
    MenuState(initialOpen = initialOpen, itemCount = itemCount)
}.apply {
    this.controlledOpen = controlledOpen
    this.onOpenChange = onOpenChange
}
