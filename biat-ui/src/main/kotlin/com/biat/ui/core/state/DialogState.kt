package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Logic-only state for Dialog. No UI, no styling.
 *
 * Behavior contract:
 * - [open] shows content, [close] hides it, [toggle] flips.
 *
 * Openness contract (controlled vs uncontrolled):
 * - Uncontrolled (default): pass [initialOpen]; the state owns the value and
 *   [open]/[close]/[toggle]/[setOpen] mutate it, notifying [onOpenChange].
 * - Controlled: pass non-null [controlledOpen] plus [onOpenChange]; the caller
 *   owns the value and [open]/[close]/[toggle]/[setOpen] only notify via
 *   [onOpenChange] without mutating. The caller must reflect the new value
 *   back into [controlledOpen] ([rememberDialogState] does this every
 *   recomposition). Do not switch modes during the state's lifetime.
 */
@Stable
class DialogState(
    initialOpen: Boolean = false,
    controlledOpen: Boolean? = null,
    var onOpenChange: ((Boolean) -> Unit)? = null,
) {
    private var internalOpen by mutableStateOf(initialOpen)

    /**
     * Caller-owned value in controlled mode. Managed by [rememberDialogState];
     * assign only to reflect the caller's value.
     */
    var controlledOpen: Boolean? by mutableStateOf(controlledOpen)

    val isOpen: Boolean get() = controlledOpen ?: internalOpen

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
}

@Composable
fun rememberDialogState(
    initialOpen: Boolean = false,
    controlledOpen: Boolean? = null,
    onOpenChange: ((Boolean) -> Unit)? = null,
): DialogState =
    remember {
        DialogState(initialOpen = initialOpen)
    }.apply {
        this.controlledOpen = controlledOpen
        this.onOpenChange = onOpenChange
    }
