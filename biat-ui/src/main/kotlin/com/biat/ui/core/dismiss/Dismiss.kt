package com.biat.ui.core.dismiss

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
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
 * Outside-press dismiss for full-screen scrim containers.
 * The scrim itself consumes taps (calls [onOutsideClick]); content must
 * consume its own taps so they don't bubble to the scrim.
 */
fun Modifier.outsideClick(onOutsideClick: () -> Unit): Modifier =
    this.pointerInput(onOutsideClick) {
        detectTapGestures(onPress = { onOutsideClick() })
    }

/** Modifier for overlay content: stop taps from reaching the scrim. */
fun Modifier.consumeOverlayTaps(): Modifier =
    this.pointerInput(Unit) {
        detectTapGestures(onPress = { /* consume, do nothing */ })
    }
