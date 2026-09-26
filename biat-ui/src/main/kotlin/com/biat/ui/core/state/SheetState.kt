package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/** Overlay sheet state. [expanded] distinguishes half/peek vs full when open. */
@Stable
class SheetState(
    initialOpen: Boolean = false,
    initialExpanded: Boolean = false,
    val onOpenChange: ((Boolean) -> Unit)? = null,
) {
    var isOpen by mutableStateOf(initialOpen)
        private set
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
        isOpen = open
        onOpenChange?.invoke(open)
    }

    fun expand() { isExpanded = true }
    fun collapse() { isExpanded = false }
}

@Composable
fun rememberSheetState(
    initialOpen: Boolean = false,
    initialExpanded: Boolean = false,
    onOpenChange: ((Boolean) -> Unit)? = null,
): SheetState = remember {
    SheetState(initialOpen, initialExpanded, onOpenChange)
}
