package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class TooltipState(
    initialVisible: Boolean = false,
) {
    var isVisible by mutableStateOf(initialVisible)
        private set

    fun show() { isVisible = true }
    fun hide() { isVisible = false }
}

@Composable
fun rememberTooltipState(initialVisible: Boolean = false): TooltipState =
    remember { TooltipState(initialVisible) }
