package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Overlay sheet state. [isExpanded] distinguishes half/peek vs full when open.
 *
 * Openness contract (controlled vs uncontrolled):
 * - Uncontrolled (default): pass [initialOpen]; the state owns the value and
 *   [open]/[close]/[toggle]/[setOpen] mutate it, notifying [onOpenChange].
 * - Controlled: pass non-null [controlledOpen] plus [onOpenChange]; the caller
 *   owns the value and [open]/[close]/[toggle]/[setOpen] only notify via
 *   [onOpenChange] without mutating. The caller must reflect the new value
 *   back into [controlledOpen] ([rememberSheetState] does this every
 *   recomposition). Do not switch modes during the state's lifetime.
 * - [isExpanded] stays internal in both modes (detent ownership lands in v0.3.0).
 */
@Stable
class SheetState(
    initialOpen: Boolean = false,
    initialExpanded: Boolean = false,
    controlledOpen: Boolean? = null,
    var onOpenChange: ((Boolean) -> Unit)? = null,
) {
    private var internalOpen by mutableStateOf(initialOpen)

    /**
     * Caller-owned value in controlled mode. Managed by [rememberSheetState];
     * assign only to reflect the caller's value.
     */
    var controlledOpen: Boolean? by mutableStateOf(controlledOpen)

    val isOpen: Boolean get() = controlledOpen ?: internalOpen
    var isExpanded by mutableStateOf(initialExpanded)
        private set

    fun open(expanded: Boolean = false) {
        isExpanded = expanded
        setOpen(true)
    }
    fun close() = setOpen(false)
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

    fun expand() { isExpanded = true }
    fun collapse() { isExpanded = false }
}

@Composable
fun rememberSheetState(
    initialOpen: Boolean = false,
    initialExpanded: Boolean = false,
    controlledOpen: Boolean? = null,
    onOpenChange: ((Boolean) -> Unit)? = null,
): SheetState = remember {
    SheetState(initialOpen = initialOpen, initialExpanded = initialExpanded)
}.apply {
    this.controlledOpen = controlledOpen
    this.onOpenChange = onOpenChange
}
