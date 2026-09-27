package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class TabsState(
    initialSelected: Any? = null,
    val onSelectedChange: ((Any?) -> Unit)? = null,
) {
    var selectedValue: Any? by mutableStateOf(initialSelected)
        private set

    fun select(value: Any?) {
        selectedValue = value
        onSelectedChange?.invoke(value)
    }

    fun isSelected(value: Any?): Boolean = selectedValue == value
}

/**
 * Keyboard move for tab lists. Callers map arrow/Home/End keys to a move
 * (honouring orientation and layout direction), then resolve it with
 * [resolveTabIndex].
 */
enum class TabMove {
    Next,
    Previous,
    First,
    Last,
}

/**
 * Pure tab-index math: [Next]/[Previous] wrap around, [First]/[Last] jump.
 * Out-of-range [current] is clamped. [size] must be positive.
 */
fun resolveTabIndex(current: Int, size: Int, move: TabMove): Int {
    require(size > 0) { "tabs must not be empty" }
    val safe = current.coerceIn(0, size - 1)
    return when (move) {
        TabMove.Next -> (safe + 1) % size
        TabMove.Previous -> (safe - 1).mod(size)
        TabMove.First -> 0
        TabMove.Last -> size - 1
    }
}

@Composable
fun rememberTabsState(
    initialSelected: Any? = null,
    onSelectedChange: ((Any?) -> Unit)? = null,
): TabsState = remember {
    TabsState(initialSelected, onSelectedChange)
}
