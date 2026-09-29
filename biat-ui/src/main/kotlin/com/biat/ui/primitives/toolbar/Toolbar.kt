package com.biat.ui.primitives.toolbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.biat.ui.core.accessibility.toolbarItemSemantics
import com.biat.ui.core.accessibility.toolbarSemantics
import com.biat.ui.core.state.ToolbarOrientation
import com.biat.ui.core.state.ToolbarState
import com.biat.ui.core.state.rememberToolbarState

/**
 * Caller key for one toolbar item. [value] is caller-owned; [label] is
 * announced on the button for screen readers.
 */
data class ToolbarValue<T>(val value: T, val label: String)

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
    if (items.isEmpty()) return
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val requesters = remember(items.size) { List(items.size) { FocusRequester() } }

    val container = Modifier
        .toolbarSemantics(label)
        .onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
            val delta: Int = when (event.key) {
                Key.DirectionRight ->
                    if (orientation == ToolbarOrientation.Horizontal) {
                        if (isRtl) -1 else 1
                    } else null
                Key.DirectionLeft ->
                    if (orientation == ToolbarOrientation.Horizontal) {
                        if (isRtl) 1 else -1
                    } else null
                Key.DirectionDown ->
                    if (orientation == ToolbarOrientation.Vertical) 1 else null
                Key.DirectionUp ->
                    if (orientation == ToolbarOrientation.Vertical) -1 else null
                Key.MoveHome -> {
                    state.moveToFirst()
                    requesters.getOrNull(state.focusedIndex)?.requestFocus()
                    return@onPreviewKeyEvent true
                }
                Key.MoveEnd -> {
                    state.moveToLast()
                    requesters.getOrNull(state.focusedIndex)?.requestFocus()
                    return@onPreviewKeyEvent true
                }
                else -> null
            } ?: return@onPreviewKeyEvent false
            state.move(delta)
            requesters.getOrNull(state.focusedIndex)?.requestFocus()
            true
        }

    val rows: @Composable () -> Unit = {
        items.forEachIndexed { index, entry ->
            ToolbarItem(
                requester = requesters[index],
                enabled = state.isEnabled(index),
                label = entry.label,
                onActivate = { onActivate(entry.value) },
            ) {
                item(entry, state.focusedIndex == index)
            }
        }
    }
    if (orientation == ToolbarOrientation.Horizontal) {
        Row(modifier = container) { rows() }
    } else {
        Column(modifier = container) { rows() }
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
    var modifier = Modifier.toolbarItemSemantics(enabled = enabled, label = label)
    if (requester != null) modifier = modifier.focusRequester(requester)
    Box(
        modifier = modifier.clickable(
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
    val state = rememberToolbarState(
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
