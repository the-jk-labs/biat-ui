package com.biat.ui.primitives.toolbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.biat.ui.core.accessibility.toolbarItemSemantics
import com.biat.ui.core.accessibility.toolbarSemantics
import com.biat.ui.core.focus.FocusGroup
import com.biat.ui.core.focus.FocusGroupEffect
import com.biat.ui.core.focus.LocalFocusGroup
import com.biat.ui.core.focus.focusGroupItem
import com.biat.ui.core.focus.focusGroupKeys
import com.biat.ui.core.state.ToolbarOrientation
import com.biat.ui.core.state.ToolbarState
import com.biat.ui.core.state.rememberToolbarState

/**
 * Caller key for one toolbar item. [value] is caller-owned; [label] is
 * announced on the button for screen readers.
 */
data class ToolbarValue<T>(
    val value: T,
    val label: String,
)

/**
 * Headless Toolbar. Behavior only, zero styling.
 *
 * Roving-tabindex container: arrows move focus with wrap-around, Home/End
 * jump; disabled items are skipped. Activation stays caller-owned via
 * [onActivate] (click also activates). [orientation] selects the arrow axis;
 * horizontal mode is RTL-aware. Items are caller visuals entirely.
 */
@Composable
fun <T> Toolbar(
    state: ToolbarState,
    items: List<ToolbarValue<T>>,
    label: String? = null,
    orientation: ToolbarOrientation = ToolbarOrientation.Horizontal,
    onActivate: (T) -> Unit = {},
    item: @Composable (item: ToolbarValue<T>, focused: Boolean) -> Unit,
) {
    val group = remember { FocusGroup() }
    group.onMove = { state.focus(it) }
    group.onFocused = { if (it >= 0) state.focus(it) }
    group.loop = state.loop
    group.initialIndex = state.focusedIndex
    FocusGroupEffect(group)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val container =
        Modifier.toolbarSemantics(label).focusGroupKeys(
            group,
            horizontal = orientation == ToolbarOrientation.Horizontal,
            rtl = rtl,
        )
    val rows: @Composable () -> Unit = {
        items.forEachIndexed { index, entry ->
            ToolbarItem(
                enabled = state.isEnabled(index),
                label = entry.label,
                onActivate = {
                    state.focus(index)
                    onActivate(entry.value)
                },
            ) {
                item(entry, state.focusedIndex == index)
            }
        }
    }
    CompositionLocalProvider(LocalFocusGroup provides group) {
        if (orientation == ToolbarOrientation.Horizontal) {
            Row(modifier = container) { rows() }
        } else {
            Column(modifier = container) { rows() }
        }
    }
}

/**
 * One toolbar button: click activates via [onActivate]. Focus is driven by
 * the parent [Toolbar] roving state through [requester].
 */
@Composable
fun ToolbarItem(
    requester: FocusRequester? = null,
    enabled: Boolean = true,
    label: String? = null,
    onActivate: () -> Unit,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    var modifier = focusGroupItem(enabled).toolbarItemSemantics(enabled = enabled, label = label)
    if (requester != null) modifier = modifier.focusRequester(requester)
    Box(
        modifier =
            modifier.clickable(
                interactionSource = source,
                indication = null,
                enabled = enabled,
                onClick = onActivate,
            ),
    ) {
        content()
    }
}

/**
 * Convenience overload owning its [ToolbarState] for the common case.
 */
@Composable
fun <T> Toolbar(
    items: List<ToolbarValue<T>>,
    label: String? = null,
    orientation: ToolbarOrientation = ToolbarOrientation.Horizontal,
    disabledIndices: Set<Int> = emptySet(),
    onActivate: (T) -> Unit = {},
    item: @Composable (item: ToolbarValue<T>, focused: Boolean) -> Unit,
) {
    val state =
        rememberToolbarState(
            itemCount = items.size,
            disabledIndices = disabledIndices,
        )
    Toolbar(
        state = state,
        items = items,
        label = label,
        orientation = orientation,
        onActivate = onActivate,
        item = item,
    )
}
