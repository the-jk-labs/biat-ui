package com.biat.ui.core.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester

/**
 * Focus return for overlays (Dialog / Sheet / Popover / Menu / Select).
 *
 * [FocusReturnEffect] restores focus to [returnRequester] on open-to-close
 * transitions only, regardless of how the overlay was dismissed (ESC, item,
 * outside tap, back press). Attach [returnRequester] to the trigger or anchor
 * via Modifier.focusRequester. Missing or detached anchors are ignored.
 * Style-free: behavior only, no visuals.
 */
@Composable
fun rememberFocusReturnRequester(): FocusRequester = remember { FocusRequester() }

@Composable
fun FocusReturnEffect(isOpen: Boolean, returnRequester: FocusRequester) {
    var wasOpen by remember { mutableStateOf(isOpen) }
    LaunchedEffect(isOpen) {
        if (shouldReturnFocus(wasOpen, isOpen)) {
            try {
                returnRequester.requestFocus()
            } catch (_: IllegalStateException) {
                // Anchor not laid out; nothing to restore.
            }
        }
        wasOpen = isOpen
    }
}

/** Open-to-close transition predicate. Extracted for unit testing. */
internal fun shouldReturnFocus(wasOpen: Boolean, isOpen: Boolean): Boolean =
    wasOpen && !isOpen
