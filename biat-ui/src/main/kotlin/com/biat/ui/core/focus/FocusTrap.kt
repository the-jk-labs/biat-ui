package com.biat.ui.core.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Focus trap for modal overlays (Dialog / Sheet).
 *
 * - [FocusTrapEffect] auto-focuses [trapRequester] while active.
 * - Tab / Shift+Tab cycles focus within the trap: after each move, the trap
 *   checks [FocusTrapState.focusInside]. On boundary ([FocusManager.moveFocus]
 *   returns false) or escape (focus left the trap), focus pulls back to the
 *   trap root, which lands on the first item. Forward wrap stops there;
 *   backward wrap then walks forward to the last item, pulling back again if
 *   the walk itself escapes. Tab is always consumed while active. Pass
 *   [trapState] (from [rememberFocusTrapState]) for escape detection and
 *   backward wrap; without it only forward boundary wrap applies.
 * - Escape is forwarded to [onEscape] (usually dismiss).
 * - Style-free: behavior only, no visuals.
 */
@Stable
class FocusTrapState {
    /** True while focus is on the trap root or any descendant. */
    internal var focusInside by mutableStateOf(false)
}

@Composable
fun rememberFocusTrapState(): FocusTrapState = remember { FocusTrapState() }

@Composable
fun rememberFocusTrapRequester(): FocusRequester = remember { FocusRequester() }

fun Modifier.focusTrap(
    active: Boolean,
    trapRequester: FocusRequester,
    focusManager: FocusManager,
    trapState: FocusTrapState? = null,
    onEscape: (() -> Unit)? = null,
): Modifier {
    if (!active) return this
    return this
        .focusRequester(trapRequester)
        .let { m ->
            if (trapState != null) {
                m.onFocusChanged { trapState.focusInside = it.hasFocus }
            } else {
                m
            }
        }
        .onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
            when (event.key) {
                Key.Tab -> {
                    val backward = event.isShiftPressed
                    val direction =
                        if (backward) FocusDirection.Previous else FocusDirection.Next
                    val moved = focusManager.moveFocus(direction)
                    val escaped = trapState != null && !trapState.focusInside
                    if (!moved || escaped) {
                        // Pull back to the root, which focuses the first item.
                        trapRequester.requestFocus()
                        if (backward && trapState != null) {
                            // Walk forward to the last item. The walk exits on
                            // boundary or escape; an escaped walk overshoots by
                            // one, so replay all but the last step from first.
                            var steps = 0
                            while (trapState.focusInside &&
                                focusManager.moveFocus(FocusDirection.Next)
                            ) {
                                steps++
                            }
                            if (!trapState.focusInside && steps > 0) {
                                trapRequester.requestFocus()
                                repeat(steps - 1) {
                                    focusManager.moveFocus(FocusDirection.Next)
                                }
                            }
                        }
                    }
                    true
                }
                Key.Escape -> {
                    onEscape?.invoke()
                    onEscape != null
                }
                else -> false
            }
        }
}

@Composable
fun FocusTrapEffect(active: Boolean, trapRequester: FocusRequester) {
    LaunchedEffect(active) {
        if (active) {
            try {
                trapRequester.requestFocus()
            } catch (_: IllegalStateException) {
                // Not attached yet; caller ordering decides.
            }
        }
    }
}
