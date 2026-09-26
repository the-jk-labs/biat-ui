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
 */
@Stable
class SelectState<T>(
    initialOpen: Boolean = false,
    initialSelected: T? = null,
    val onOpenChange: ((Boolean) -> Unit)? = null,
    val onSelectedChange: ((T?) -> Unit)? = null,
) {
    var isOpen by mutableStateOf(initialOpen)
        private set
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
        isOpen = open
        onOpenChange?.invoke(open)
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
    onOpenChange: ((Boolean) -> Unit)? = null,
    onSelectedChange: ((T?) -> Unit)? = null,
): SelectState<T> = remember {
    SelectState(initialOpen, initialSelected, onOpenChange, onSelectedChange)
}
