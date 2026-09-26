package com.biat.ui.core.dismiss

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput

/**
 * ESC-to-dismiss. Attach to overlay content.
 */
fun Modifier.onEscape(onEscape: () -> Unit): Modifier =
    this.onPreviewKeyEvent { event ->
        if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
            onEscape()
            true
        } else {
            false
        }
    }

/**
 * Outside-tap dismiss for full-screen scrim containers.
 * Fires on tap-up (press-drag-release cancels); content taps are excluded
 * two ways: [consumeOverlayTaps] on the content, and the [isInsideContent]
 * geometry check (tap offset in scrim coordinates). The geometry check is
 * the reliable one: it holds even when content fills the scrim container
 * (e.g. a centered card via fillMaxSize), where consumption alone cannot
 * distinguish scrim from content.
 */
fun Modifier.outsideClick(
    onOutsideClick: () -> Unit,
    isInsideContent: ((Offset) -> Boolean)? = null,
): Modifier =
    this.pointerInput(onOutsideClick, isInsideContent) {
        detectTapGestures(
            onTap = { offset ->
                if (isInsideContent?.invoke(offset) != true) onOutsideClick()
            },
        )
    }

/**
 * Modifier for overlay content: stop taps from reaching the scrim.
 * Consumes the whole gesture (down through up) inside content bounds, so
 * ancestor tap detectors cancel. Kept as defense in depth alongside the
 * [outsideClick] geometry check.
 */
fun Modifier.consumeOverlayTaps(): Modifier =
    this.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown()
            down.consume()
            do {
                val event = awaitPointerEvent()
                event.changes.forEach { it.consume() }
            } while (event.changes.any { it.pressed })
        }
    }
