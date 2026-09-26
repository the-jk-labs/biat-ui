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
 * - [onOpenChange] is the single source of truth hook for controlled usage.
 */
@Stable
class DialogState(
    initialOpen: Boolean = false,
    val onOpenChange: ((Boolean) -> Unit)? = null,
) {
    var isOpen by mutableStateOf(initialOpen)
        private set

    fun open() = setOpen(true)
    fun close() = setOpen(false)
    fun toggle() = setOpen(!isOpen)

    @JvmName("setOpenState")
    fun setOpen(open: Boolean) {
        if (isOpen == open) return
        isOpen = open
        onOpenChange?.invoke(open)
    }
}

@Composable
fun rememberDialogState(
    initialOpen: Boolean = false,
    onOpenChange: ((Boolean) -> Unit)? = null,
): DialogState = remember {
    DialogState(initialOpen, onOpenChange)
}
