package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Logic-only state for a headless radio group. No UI, no styling.
 *
 * Holds the selected key ([Any]) or null for none. Selection stays
 * uncontrolled; every change is mirrored to [onSelectedChange].
 * When [enabled] is false [select]/[clear] are ignored entirely.
 * Radio semantics mean selecting the current value is a no-op (no deselect).
 */
@Stable
class RadioGroupState(
    initialSelected: Any? = null,
    var enabled: Boolean = true,
    var onSelectedChange: ((Any?) -> Unit)? = null,
) {
    var selectedValue: Any? by mutableStateOf(initialSelected)
        private set

    fun select(value: Any?) {
        if (!enabled || selectedValue == value) return
        selectedValue = value
        onSelectedChange?.invoke(value)
    }

    fun clear() {
        if (!enabled || selectedValue == null) return
        selectedValue = null
        onSelectedChange?.invoke(null)
    }

    fun isSelected(value: Any?): Boolean = selectedValue == value
}

/**
 * Remembers [RadioGroupState] across recompositions. [enabled] and
 * [onSelectedChange] are refreshed every recomposition so handlers never
 * go stale.
 */
@Composable
fun rememberRadioGroupState(
    initialSelected: Any? = null,
    enabled: Boolean = true,
    onSelectedChange: ((Any?) -> Unit)? = null,
): RadioGroupState =
    remember {
        RadioGroupState(initialSelected = initialSelected, enabled = enabled)
    }.apply {
        this.enabled = enabled
        this.onSelectedChange = onSelectedChange
    }
