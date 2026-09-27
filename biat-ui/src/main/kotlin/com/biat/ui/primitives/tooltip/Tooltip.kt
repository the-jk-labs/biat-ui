package com.biat.ui.primitives.tooltip

import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.biat.ui.core.accessibility.tooltipAnchorSemantics
import com.biat.ui.core.positioning.PopupAlign
import com.biat.ui.core.positioning.PopupSide
import com.biat.ui.core.positioning.rememberBiatPopupPosition
import com.biat.ui.core.state.TooltipState
import com.biat.ui.core.state.rememberTooltipState
import kotlinx.coroutines.delay

/**
 * Headless Tooltip. Shows [overlay] on hover / focus after [showDelayMs],
 * or on touch long-press when [enableLongPress]. Placement follows
 * [side]/[align] with [sideOffset]/[alignOffset] gaps; [avoidCollisions]
 * flips to the opposite side when it overflows less and shifts the tip to
 * stay on-screen. Zero styling; caller owns overlay visuals.
 */
@Composable
fun Tooltip(
    state: TooltipState = rememberTooltipState(),
    tip: String,
    showDelayMs: Long = 300,
    side: PopupSide = PopupSide.Top,
    align: PopupAlign = PopupAlign.Center,
    sideOffset: Dp = 0.dp,
    alignOffset: Dp = 0.dp,
    avoidCollisions: Boolean = true,
    enableLongPress: Boolean = true,
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
                }
                .then(
                    if (enableLongPress) {
                        Modifier.pointerInput(state) {
                            detectTapGestures(onLongPress = { state.show() })
                        }
                    } else {
                        Modifier
                    },
                ),
        ) {
            anchor()
        }
        if (state.isVisible) {
            // No focus return: the overlay is not focusable, so focus never
            // leaves the anchor while the tip is visible.
            Popup(
                popupPositionProvider = rememberBiatPopupPosition(
                    side = side,
                    align = align,
                    sideOffset = sideOffset,
                    alignOffset = alignOffset,
                    avoidCollisions = avoidCollisions,
                ),
                onDismissRequest = { state.hide() },
                properties = PopupProperties(focusable = false),
            ) {
                overlay()
            }
        }
    }
}
