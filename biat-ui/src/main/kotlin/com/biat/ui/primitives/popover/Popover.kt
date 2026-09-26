package com.biat.ui.primitives.popover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.biat.ui.core.dismiss.onEscape
import com.biat.ui.core.focus.FocusReturnEffect
import com.biat.ui.core.focus.rememberFocusReturnRequester
import com.biat.ui.core.state.PopoverState
import com.biat.ui.core.state.rememberPopoverState

/**
 * Headless Popover. Non-modal overlay anchored to [trigger].
 * Zero styling; positioning via Popup defaults (caller may wrap content).
 * Focus returns to the trigger whenever the popover closes.
 */
@Composable
fun Popover(
    state: PopoverState = rememberPopoverState(),
    dismissOnOutsideClick: Boolean = true,
    dismissOnEscape: Boolean = true,
    dismissOnBackPress: Boolean = true,
    trigger: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Box {
        val source = remember { MutableInteractionSource() }
        val returnRequester = rememberFocusReturnRequester()
        FocusReturnEffect(isOpen = state.isOpen, returnRequester = returnRequester)
        Box(
            modifier = Modifier
                .focusRequester(returnRequester)
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
                onDismissRequest = { state.close() },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = dismissOnBackPress,
                    dismissOnClickOutside = dismissOnOutsideClick,
                ),
            ) {
                Box(
                    modifier = Modifier.let { m ->
                        if (dismissOnEscape) m.onEscape { state.close() } else m
                    },
                ) {
                    content()
                }
            }
        }
    }
}
