package com.biat.ui.primitives.separator

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.biat.ui.core.accessibility.separatorSemantics

/**
 * Headless Separator. Behavior only, zero styling.
 *
 * A non-interactive boundary owned visually by the caller. Decorative
 * separators (the default) are skipped by assistive tech; set [decorative]
 * to false to announce orientation.
 */
@Composable
fun Separator(
    decorative: Boolean = true,
    vertical: Boolean = false,
    content: @Composable () -> Unit,
) {
    Box(modifier = Modifier.separatorSemantics(decorative = decorative, vertical = vertical)) {
        content()
    }
}
