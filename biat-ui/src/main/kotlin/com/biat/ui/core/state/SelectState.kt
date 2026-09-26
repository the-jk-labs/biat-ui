package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Generic single-select state backing Select / Combobox.
 *
 * Openness contract (controlled vs uncontrolled):
 * - Uncontrolled (default): pass [initialOpen]; the state owns the value and
 *   [open]/[close]/[toggle]/[setOpen] mutate it, notifying [onOpenChange].
 * - Controlled: pass non-null [controlledOpen] plus [onOpenChange]; the caller
 *   owns the value and [open]/[close]/[toggle]/[setOpen] only notify via
 *   [onOpenChange] without mutating. The caller must reflect the new value
 *   back into [controlledOpen] ([rememberSelectState] does this every
 *   recomposition). Do not switch modes during the state's lifetime.
 * - Selection itself ([select]/[clearSelection]) stays uncontrolled; the
 *   committed value is mirrored to [onSelectedChange].
 */
@Stable
class SelectState<T>(
    initialOpen: Boolean = false,
    initialSelected: T? = null,
    controlledOpen: Boolean? = null,
    var onOpenChange: ((Boolean) -> Unit)? = null,
    val onSelectedChange: ((T?) -> Unit)? = null,
) {
    private var internalOpen by mutableStateOf(initialOpen)

    /**
     * Caller-owned value in controlled mode. Managed by [rememberSelectState];
     * assign only to reflect the caller's value.
     */
    var controlledOpen: Boolean? by mutableStateOf(controlledOpen)

    val isOpen: Boolean get() = controlledOpen ?: internalOpen
    var selected: T? by mutableStateOf(initialSelected)
        private set
    var highlightedIndex by mutableIntStateOf(-1)
        internal set

    fun open() = setOpen(true)
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

    fun select(value: T?) {
        selected = value
        onSelectedChange?.invoke(value)
        close()
    }

    fun clearSelection() = select(null)
}

@Composable
fun <T> rememberSelectState(
    initialOpen: Boolean = false,
    initialSelected: T? = null,
    controlledOpen: Boolean? = null,
    onOpenChange: ((Boolean) -> Unit)? = null,
    onSelectedChange: ((T?) -> Unit)? = null,
): SelectState<T> = remember {
    SelectState<T>(
        initialOpen = initialOpen,
        initialSelected = initialSelected,
        onSelectedChange = onSelectedChange,
    )
}.apply {
    this.controlledOpen = controlledOpen
    this.onOpenChange = onOpenChange
}
