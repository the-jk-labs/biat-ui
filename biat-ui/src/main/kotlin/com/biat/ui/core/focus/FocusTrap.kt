package com.biat.ui.core.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
 * - Tab / Shift+Tab moves focus within the trap instead of escaping it.
 * - Escape is forwarded to [onEscape] (usually dismiss).
 * - Style-free: behavior only, no visuals.
 */
@Composable
fun rememberFocusTrapRequester(): FocusRequester = remember { FocusRequester() }

fun Modifier.focusTrap(
    active: Boolean,
    trapRequester: FocusRequester,
    focusManager: FocusManager,
    onEscape: (() -> Unit)? = null,
): Modifier {
    if (!active) return this
    return this
        .focusRequester(trapRequester)
        .onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
            when (event.key) {
                Key.Tab -> {
                    val direction =
                        if (event.isShiftPressed) FocusDirection.Previous else FocusDirection.Next
                    focusManager.moveFocus(direction)
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
