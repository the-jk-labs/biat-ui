package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Logic-only state for Tooltip. No UI, no styling.
 *
 * Visibility contract (controlled vs uncontrolled), same shape as the
 * open/close contract on the other states under show/hide naming:
 * - Uncontrolled (default): pass [initialVisible]; the state owns the value.
 * - Controlled: pass non-null [controlledVisible] plus [onVisibleChange]; the
 *   caller owns the value and [show]/[hide] only notify without mutating.
 *   The caller must reflect the new value back into [controlledVisible]
 *   ([rememberTooltipState] does this every recomposition). Do not switch
 *   modes during the state's lifetime.
 */
@Stable
class TooltipState(
    initialVisible: Boolean = false,
    controlledVisible: Boolean? = null,
    var onVisibleChange: ((Boolean) -> Unit)? = null,
) {
    private var internalVisible by mutableStateOf(initialVisible)

    /**
     * Caller-owned value in controlled mode. Managed by
     * [rememberTooltipState]; assign only to reflect the caller's value.
     */
    var controlledVisible: Boolean? by mutableStateOf(controlledVisible)

    val isVisible: Boolean get() = controlledVisible ?: internalVisible

    fun show() = setVisible(true)

    fun hide() = setVisible(false)

    fun setVisible(visible: Boolean) {
        if (isVisible == visible) return
        if (controlledVisible != null) {
            onVisibleChange?.invoke(visible)
        } else {
            internalVisible = visible
            onVisibleChange?.invoke(visible)
        }
    }
}

@Composable
fun rememberTooltipState(
    initialVisible: Boolean = false,
    controlledVisible: Boolean? = null,
    onVisibleChange: ((Boolean) -> Unit)? = null,
): TooltipState =
    remember {
        TooltipState(initialVisible = initialVisible)
    }.apply {
        this.controlledVisible = controlledVisible
        this.onVisibleChange = onVisibleChange
    }
