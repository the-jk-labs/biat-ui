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
 */
@Stable
class MenuState(
    initialOpen: Boolean = false,
    val itemCount: Int = 0,
    val onOpenChange: ((Boolean) -> Unit)? = null,
) {
    var isOpen by mutableStateOf(initialOpen)
        private set

    /** Index highlighted for keyboard / hover. -1 = none. */
    var highlightedIndex by mutableIntStateOf(-1)
        private set

    fun open() = setOpen(true)
    fun close() {
        setOpen(false)
        highlightedIndex = -1
    }
    fun toggle() = setOpen(!isOpen)

    @JvmName("setOpenState")
    fun setOpen(open: Boolean) {
        if (isOpen == open) return
        isOpen = open
        if (!open) highlightedIndex = -1
        onOpenChange?.invoke(open)
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
    onOpenChange: ((Boolean) -> Unit)? = null,
): MenuState = remember(itemCount) {
    MenuState(initialOpen, itemCount, onOpenChange)
}
