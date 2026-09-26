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

@Composable
fun rememberTabsState(
    initialSelected: Any? = null,
    onSelectedChange: ((Any?) -> Unit)? = null,
): TabsState = remember {
    TabsState(initialSelected, onSelectedChange)
}
