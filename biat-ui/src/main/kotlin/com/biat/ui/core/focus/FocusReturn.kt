package com.biat.ui.core.focus

import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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

/**
 * Returns focus to [returnRequester] on open-to-close transitions of [isOpen].
 * Attach the requester to the overlay trigger via Modifier.focusRequester.
 * Missing or detached triggers are ignored instead of crashing.
 */
@Composable
fun FocusReturnEffect(
    isOpen: Boolean,
    returnRequester: FocusRequester,
) {
    val view = LocalView.current
    var wasOpen by remember { mutableStateOf(isOpen) }
    LaunchedEffect(isOpen) {
        if (shouldReturnFocus(wasOpen, isOpen)) {
            awaitWindowFocus(view)
            withFrameNanos { }
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
internal fun shouldReturnFocus(
    wasOpen: Boolean,
    isOpen: Boolean,
): Boolean = wasOpen && !isOpen

/**
 * Requests focus, ignoring detached nodes. Roving containers (Menu / Tabs /
 * Toolbar / Select lists) call this on key paths that can run before layout.
 * Returns true when the target accepts focus.
 */
fun FocusRequester.safeRequestFocus(): Boolean =
    try {
        requestFocus()
    } catch (_: IllegalStateException) {
        false
    }

/** Waits for the host window to regain focus after a popup or dialog is removed. */
internal suspend fun awaitWindowFocus(view: View) {
    if (view.hasWindowFocus()) return
    suspendCancellableCoroutine<Unit> { continuation ->
        val observer = view.viewTreeObserver
        lateinit var listener: ViewTreeObserver.OnWindowFocusChangeListener
        listener =
            ViewTreeObserver.OnWindowFocusChangeListener { focused ->
                if (focused && continuation.isActive) {
                    if (observer.isAlive) observer.removeOnWindowFocusChangeListener(listener)
                    continuation.resume(Unit)
                }
            }
        observer.addOnWindowFocusChangeListener(listener)
        continuation.invokeOnCancellation {
            if (observer.isAlive) observer.removeOnWindowFocusChangeListener(listener)
        }
        if (view.hasWindowFocus() && continuation.isActive) {
            observer.removeOnWindowFocusChangeListener(listener)
            continuation.resume(Unit)
        }
    }
}
