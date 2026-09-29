package com.biat.ui.primitives.popover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.biat.ui.core.dismiss.onEscape
import com.biat.ui.core.accessibility.overlayTriggerSemantics
import com.biat.ui.core.focus.FocusReturnEffect
import com.biat.ui.core.focus.FocusTrapEffect
import com.biat.ui.core.focus.focusTrap
import com.biat.ui.core.focus.rememberFocusReturnRequester
import com.biat.ui.core.focus.rememberFocusTrapRequester
import com.biat.ui.core.focus.rememberFocusTrapState
import com.biat.ui.core.positioning.PopupAlign
import com.biat.ui.core.positioning.PopupSide
import com.biat.ui.core.positioning.rememberBiatPopupPosition
import com.biat.ui.core.state.PopoverState
import com.biat.ui.core.state.rememberPopoverState

/**
 * Headless Popover. Overlay anchored to [trigger].
 * Zero styling; the caller owns every pixel inside [content].
 * Placement follows [side]/[align] with [sideOffset]/[alignOffset] gaps;
 * [avoidCollisions] flips to the opposite side when it overflows less and
 * shifts the popup to stay on-screen. Focus returns to the trigger
 * whenever the popover closes.
 *
 * - [modal]: traps Tab focus inside the popup and moves initial focus to
 *   the popup root on open. Non-modal (the default) leaves focus alone.
 *   Either way callers render their own scrim; outside-click dismissal is
 *   still governed by [dismissOnOutsideClick].
 * - [label] names the trigger for screen readers (announced with
 *   expanded/collapsed state).
 *
  * Anchor-follow on scroll/resize comes from the platform: Popup re-resolves
  * the shared placement engine against fresh anchor bounds whenever the
  * anchor moves, so the popup tracks scrolling content with no extra API.
  */
@Composable
fun Popover(
    state: PopoverState = rememberPopoverState(),
    dismissOnOutsideClick: Boolean = true,
    dismissOnEscape: Boolean = true,
    dismissOnBackPress: Boolean = true,
    label: String? = null,
    side: PopupSide = PopupSide.Bottom,
    align: PopupAlign = PopupAlign.Start,
    sideOffset: Dp = 0.dp,
    alignOffset: Dp = 0.dp,
    avoidCollisions: Boolean = true,
    modal: Boolean = false,
    trigger: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Box {
        val source = remember { MutableInteractionSource() }
        val returnRequester = rememberFocusReturnRequester()
        val trapRequester = rememberFocusTrapRequester()
        val trapState = rememberFocusTrapState()
        FocusReturnEffect(isOpen = state.isOpen, returnRequester = returnRequester)
        FocusTrapEffect(active = modal && state.isOpen, trapRequester = trapRequester)
        Box(
            modifier = Modifier
                .focusRequester(returnRequester)
                .overlayTriggerSemantics(expanded = state.isOpen, label = label)
                .clickable(
                    interactionSource = source,
                    indication = null,
                    onClick = { state.toggle() },
                ),
        ) {
            trigger()
        }
        if (state.isOpen) {
            Popup(
                popupPositionProvider = rememberBiatPopupPosition(
                    side = side,
                    align = align,
                    sideOffset = sideOffset,
                    alignOffset = alignOffset,
                    avoidCollisions = avoidCollisions,
                ),
                onDismissRequest = { state.close() },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = dismissOnBackPress,
                    dismissOnClickOutside = dismissOnOutsideClick,
                ),
            ) {
                // FocusManager is read inside the popup: the popup window
                // owns focus separately, so the main window manager cannot
                // move focus between popup items.
                val popupFocusManager = LocalFocusManager.current
                Box(
                    modifier = Modifier.let { m ->
                        var acc = if (dismissOnEscape) m.onEscape { state.close() } else m
                        if (modal) {
                            acc = acc.focusTrap(
                                active = true,
                                trapRequester = trapRequester,
                                focusManager = popupFocusManager,
                                trapState = trapState,
                            )
                        }
                        acc
                    },
                ) {
                    content()
                }
            }
        }
    }
}
