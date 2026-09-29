package com.biat.ui.primitives.label

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.biat.ui.core.accessibility.labelSemantics

/**
 * Headless Label. Behavior only, zero styling.
 *
 * Names [controlLabel] for screen readers; clicking the label fires
 * [onClick] so callers can forward focus/activation to their control.
 * Visuals are caller-owned.
 */
@Composable
fun Label(
    controlLabel: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    var modifier = Modifier.labelSemantics(controlLabel)
    if (onClick != null) {
        modifier = modifier.clickable(
            interactionSource = source,
            indication = null,
            onClick = onClick,
        )
    }
    Box(modifier = modifier) {
        content()
    }
}
