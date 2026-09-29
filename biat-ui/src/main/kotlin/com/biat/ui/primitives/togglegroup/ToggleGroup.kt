package com.biat.ui.primitives.togglegroup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.biat.ui.core.accessibility.toggleGroupSemantics
import com.biat.ui.core.accessibility.toggleSemantics
import com.biat.ui.core.state.ToggleGroupState
import com.biat.ui.core.state.ToggleGroupType
import com.biat.ui.core.state.rememberToggleGroupState

/**
 * Caller key for one toggle-group item. [value] drives [ToggleGroupState]
 * pressed membership; [label] is announced on the button for screen readers.
 */
data class ToggleGroupValue<T>(
    val value: T,
    val label: String,
)

/**
 * Headless ToggleGroup. Behavior only, zero styling.
 *
 * Renders [items] inline; each button toggles its value in [state]. [type]
 * selects single vs multiple pressed; [allowDeselect] lets single mode clear
 * its pressed item. Buttons are caller visuals entirely.
 */
@Composable
fun <T> ToggleGroup(
    state: ToggleGroupState = rememberToggleGroupState(),
    items: List<ToggleGroupValue<T>>,
    label: String? = null,
    enabled: Boolean = true,
    horizontal: Boolean = true,
    item: @Composable (item: ToggleGroupValue<T>, pressed: Boolean) -> Unit,
) {
    val container = Modifier.toggleGroupSemantics(label)
    val rows: @Composable () -> Unit = {
        items.forEach { entry ->
            ToggleGroupItem(
                pressed = state.isPressed(entry.value as Any?),
                enabled = enabled,
                label = entry.label,
                onToggle = { state.toggle(entry.value as Any?) },
            ) {
                item(entry, state.isPressed(entry.value as Any?))
            }
        }
    }
    if (horizontal) {
        Row(modifier = container) { rows() }
    } else {
        Column(modifier = container) { rows() }
    }
}

/**
 * One toggle-group button: click toggles pressed via [onToggle].
 * Compose directly for custom layouts instead of [ToggleGroup].
 */
@Composable
fun ToggleGroupItem(
    pressed: Boolean,
    enabled: Boolean = true,
    label: String? = null,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier =
            Modifier
                .toggleSemantics(pressed = pressed, enabled = enabled, label = label)
                .clickable(
                    interactionSource = source,
                    indication = null,
                    enabled = enabled,
                    onClick = onToggle,
                ),
    ) {
        content()
    }
}

/**
 * Scope for [ToggleGroupContent]: buttons bound to a shared [state].
 * Prefer [ToggleGroup] for flat lists; use this only for custom layouts.
 */
class ToggleGroupScope internal constructor(
    private val state: ToggleGroupState,
) {
    @Composable
    fun <T> Item(
        item: ToggleGroupValue<T>,
        enabled: Boolean = true,
        content: @Composable (pressed: Boolean) -> Unit,
    ) {
        val pressed = state.isPressed(item.value as Any?)
        ToggleGroupItem(
            pressed = pressed,
            enabled = enabled,
            label = item.label,
            onToggle = { state.toggle(item.value as Any?) },
        ) {
            content(pressed)
        }
    }
}

/**
 * Custom-layout toggle group: caller arranges [ToggleGroupScope.Item] buttons
 * inside [content]. Pressed membership still lives in [state].
 */
@Composable
fun ToggleGroupContent(
    state: ToggleGroupState =
        rememberToggleGroupState(
            type = ToggleGroupType.Multiple,
        ),
    label: String? = null,
    content: @Composable ColumnScope.(scope: ToggleGroupScope) -> Unit,
) {
    Column(modifier = Modifier.toggleGroupSemantics(label)) {
        content(ToggleGroupScope(state))
    }
}
