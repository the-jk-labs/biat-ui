package com.biat.ui.primitives.tooltip

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.biat.ui.core.accessibility.tooltipAnchorSemantics
import com.biat.ui.core.state.TooltipState
import com.biat.ui.core.state.rememberTooltipState
import kotlinx.coroutines.delay

/**
 * Headless Tooltip. Shows [overlay] on hover / focus after [showDelayMs].
 * Zero styling; caller owns overlay visuals.
 */
@Composable
fun Tooltip(
    state: TooltipState = rememberTooltipState(),
    tip: String,
    showDelayMs: Long = 300,
    overlay: @Composable () -> Unit,
    anchor: @Composable () -> Unit,
) {
    val hoverSource = remember { MutableInteractionSource() }
    val isHovered by hoverSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) {
        if (isHovered) {
            delay(showDelayMs)
            state.show()
        } else {
            state.hide()
        }
    }

    Box {
        Box(
            modifier = Modifier
                .hoverable(hoverSource)
                .tooltipAnchorSemantics(tip)
                .onFocusChanged { focus ->
                    if (focus.isFocused) state.show() else state.hide()
                },
        ) {
            anchor()
        }
        if (state.isVisible) {
            // No focus return: the overlay is not focusable, so focus never
            // leaves the anchor while the tip is visible.
            Popup(
                onDismissRequest = { state.hide() },
                properties = PopupProperties(focusable = false),
            ) {
                overlay()
            }
        }
    }
}
