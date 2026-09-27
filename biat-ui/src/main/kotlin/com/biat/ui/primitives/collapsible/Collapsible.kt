package com.biat.ui.primitives.collapsible

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.biat.ui.core.accessibility.collapsibleTriggerSemantics
import com.biat.ui.core.state.CollapsibleState
import com.biat.ui.core.state.rememberCollapsibleState

/**
 * Headless Collapsible. Behavior only, zero styling.
 *
 * - [trigger] renders inline and toggles expansion on click.
 * - [content] renders inline when [CollapsibleState.isExpanded] is true.
 * - [label] is exposed as the trigger content description for screen readers.
 * - [enabled] blocks toggling entirely when false.
 */
@Composable
fun Collapsible(
    state: CollapsibleState = rememberCollapsibleState(),
    enabled: Boolean = true,
    label: String? = null,
    trigger: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        val source = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .collapsibleTriggerSemantics(expanded = state.isExpanded, label = label)
                .clickable(
                    interactionSource = source,
                    indication = null,
                    enabled = enabled,
                    onClick = { state.toggle() },
                ),
        ) {
            trigger()
        }
        if (state.isExpanded) {
            Column(content = content)
        }
    }
}
