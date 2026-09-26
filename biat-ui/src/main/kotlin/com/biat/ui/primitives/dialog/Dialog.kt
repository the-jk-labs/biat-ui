package com.biat.ui.primitives.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import com.biat.ui.core.accessibility.dialogSemantics
import com.biat.ui.core.dismiss.consumeOverlayTaps
import com.biat.ui.core.dismiss.onEscape
import com.biat.ui.core.dismiss.outsideClick
import com.biat.ui.core.focus.FocusReturnEffect
import com.biat.ui.core.focus.FocusTrapEffect
import com.biat.ui.core.focus.focusTrap
import com.biat.ui.core.focus.rememberFocusReturnRequester
import com.biat.ui.core.focus.rememberFocusTrapRequester
import com.biat.ui.core.focus.rememberFocusTrapState
import com.biat.ui.core.portal.BiatPortal
import com.biat.ui.core.state.DialogState
import com.biat.ui.core.state.rememberDialogState

/**
 * Headless Dialog. Behavior only, zero styling.
 *
 * - [trigger] renders inline and opens the dialog on click.
 * - [content] renders in a [BiatPortal] with focus trap + ESC + outside-click.
 * - Focus returns to the trigger whenever the dialog closes.
 * - The caller owns scrim visuals: wrap [content] in their own Box/surface.
 *   Set [dismissOnOutsideClick] = false if the scrim handles dismissal itself.
 */
@Composable
fun Dialog(
    state: DialogState = rememberDialogState(),
    dismissOnOutsideClick: Boolean = true,
    dismissOnEscape: Boolean = true,
    dismissOnBackPress: Boolean = true,
    scrim: @Composable (() -> Unit)? = null,
    trigger: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    if (trigger != null) {
        val returnRequester = rememberFocusReturnRequester()
        FocusReturnEffect(isOpen = state.isOpen, returnRequester = returnRequester)
        DialogTrigger(
            state = state,
            returnFocusRequester = returnRequester,
            content = trigger,
        )
    }
    if (!state.isOpen) return

    val trapRequester = rememberFocusTrapRequester()
    val trapState = rememberFocusTrapState()
    val focusManager = LocalFocusManager.current
    FocusTrapEffect(active = true, trapRequester = trapRequester)

    BiatPortal(
        onDismissRequest = { state.close() },
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = false, // handled explicitly for headless control
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .let { m ->
                    if (dismissOnOutsideClick) m.outsideClick { state.close() } else m
                },
        ) {
            scrim?.invoke()
            Box(
                modifier = Modifier
                    .consumeOverlayTaps()
                    .dialogSemantics()
                    .focusTrap(
                        active = true,
                        trapRequester = trapRequester,
                        focusManager = focusManager,
                        trapState = trapState,
                        onEscape = if (dismissOnEscape) ({ state.close() }) else null,
                    )
                    .let { m ->
                        if (dismissOnEscape) m.onEscape { state.close() } else m
                    },
            ) {
                content()
            }
        }
    }
}

/** Headless trigger: any caller UI that opens the dialog. No styling imposed. */
@Composable
fun DialogTrigger(
    state: DialogState,
    returnFocusRequester: FocusRequester? = null,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .let { m ->
                if (returnFocusRequester != null) m.focusRequester(returnFocusRequester) else m
            }
            .clickable(
                interactionSource = source,
                indication = null,
                onClick = { state.open() },
            ),
    ) {
        content()
    }
}

/** Headless close affordance: any caller UI that closes the dialog. */
@Composable
fun DialogClose(
    state: DialogState,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier.clickable(
            interactionSource = source,
            indication = null,
            onClick = { state.close() },
        ),
    ) {
        content()
    }
}
