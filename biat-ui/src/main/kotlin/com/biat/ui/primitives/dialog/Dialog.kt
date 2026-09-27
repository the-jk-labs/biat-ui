package com.biat.ui.primitives.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import com.biat.ui.core.accessibility.dialogSemantics
import com.biat.ui.core.dismiss.consumeOverlayTaps
import com.biat.ui.core.dismiss.onEscape
import com.biat.ui.core.dismiss.outsideClick
import com.biat.ui.core.focus.FocusReturnEffect
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
 * - [label] is exposed as the dialog content description for screen readers.
 *   Content text remains the caller's responsibility.
 * - [content] sizes the outside-click dismiss boundary: keep it wrap-content.
 *   A full-window content claims the whole window and disables scrim
 *   dismissal (every tap reads as inside).
 * - The caller owns scrim visuals: wrap [content] in their own Box/surface.
 *   Set [dismissOnOutsideClick] = false if the scrim handles dismissal itself.
 * - Alert variant ([isAlert] = true): explicit-action dialog. Outside-click
 *   dismissal defaults to off (pass [dismissOnOutsideClick] = true to opt
 *   back in); Esc/back still dismiss unless disabled.
 * - [initialFocusRequester]: node focused on open (e.g. a text field or the
 *   confirm button). Defaults to the trap root. Tab cycling is unaffected.
 * - Nesting: a Dialog composes inside another dialog's [content] with its
 *   own state. Windows layer naturally: Esc/back/outside-tap hit only the
 *   topmost open dialog, and focus returns down the trigger chain on close.
 */
@Composable
fun Dialog(
    state: DialogState = rememberDialogState(),
    dismissOnOutsideClick: Boolean? = null,
    dismissOnEscape: Boolean = true,
    dismissOnBackPress: Boolean = true,
    isAlert: Boolean = false,
    initialFocusRequester: FocusRequester? = null,
    label: String? = null,
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

    val effectiveOutsideClick = dismissOnOutsideClick ?: !isAlert
    val trapRequester = rememberFocusTrapRequester()
    val trapState = rememberFocusTrapState()
    val focusManager = LocalFocusManager.current
    // Requested once at first layout: the portal window is attached by then,
    // which a composition-time effect cannot guarantee. Falls back to the
    // trap root when a caller target is missing or detached.
    var initialFocusDone by remember { mutableStateOf(false) }

    // Dismiss boundary follows the content bounds, not the scrim: taps
    // inside the card are ignored even when content fills the container
    // (e.g. a centered card), where tap consumption alone cannot tell
    // scrim from content.
    var contentBounds by remember { mutableStateOf<Rect?>(null) }

    BiatPortal(
        onDismissRequest = { state.close() },
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = false, // handled explicitly for headless control
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .let { m ->
                    if (effectiveOutsideClick) {
                        m.outsideClick(
                            onOutsideClick = { state.close() },
                            isInsideContent = { offset ->
                                contentBounds?.contains(offset) == true
                            },
                        )
                    } else {
                        m
                    }
                },
        ) {
            scrim?.invoke()
            Box(
                modifier = Modifier
                    .onGloballyPositioned {
                        contentBounds = it.boundsInParent()
                        if (!initialFocusDone) {
                            initialFocusDone = true
                            val target = initialFocusRequester ?: trapRequester
                            try {
                                target.requestFocus()
                            } catch (_: IllegalStateException) {
                                if (target !== trapRequester) {
                                    try {
                                        trapRequester.requestFocus()
                                    } catch (_: IllegalStateException) {
                                        // Neither attached; caller ordering decides.
                                    }
                                }
                            }
                        }
                    }
                    .consumeOverlayTaps()
                    .dialogSemantics(label)
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
