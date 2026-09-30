package com.biat.ui.primitives.radiogroup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.biat.ui.core.accessibility.radioGroupSemantics
import com.biat.ui.core.accessibility.radioItemSemantics
import com.biat.ui.core.focus.FocusGroup
import com.biat.ui.core.focus.FocusGroupEffect
import com.biat.ui.core.focus.LocalFocusGroup
import com.biat.ui.core.focus.focusGroupItem
import com.biat.ui.core.focus.focusGroupKeys
import com.biat.ui.core.state.RadioGroupState
import com.biat.ui.core.state.rememberRadioGroupState

/**
 * Caller key for one radio item. [value] drives [RadioGroupState] selection;
 * [label] is announced on the item for screen readers.
 */
data class RadioGroupValue<T>(
    val value: T,
    val label: String,
)

/**
 * Headless RadioGroup. Behavior only, zero styling.
 *
 * Renders [options] inline; each row selects its value in [state].
 * Selecting the current value is a no-op (no deselect). [enabled] blocks
 * the whole group; [disabledValues] blocks single options.
 */
@Composable
fun <T> RadioGroup(
    state: RadioGroupState = rememberRadioGroupState(),
    options: List<RadioGroupValue<T>>,
    label: String? = null,
    enabled: Boolean = true,
    disabledValues: Set<T> = emptySet(),
    item: @Composable (item: RadioGroupValue<T>, selected: Boolean) -> Unit,
) {
    val group = remember { FocusGroup() }
    FocusGroupEffect(group)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    CompositionLocalProvider(LocalFocusGroup provides group) {
        Column(modifier = Modifier.radioGroupSemantics(label).focusGroupKeys(group, rtl = rtl)) {
            options.forEach { item ->
                RadioItem(
                    selected = state.isSelected(item.value as Any?),
                    enabled = enabled && state.enabled && item.value !in disabledValues,
                    label = item.label,
                    onSelect = { state.select(item.value as Any?) },
                ) {
                    item(item, state.isSelected(item.value as Any?))
                }
            }
        }
    }
}

/**
 * One radio row: selecting sets [selected] via [onSelect]. Compose directly
 * for custom layouts instead of [RadioGroup].
 */
@Composable
fun RadioItem(
    selected: Boolean,
    enabled: Boolean = true,
    label: String? = null,
    onSelect: () -> Unit,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier =
            focusGroupItem(enabled, preferred = selected, onNavigate = onSelect)
                .radioItemSemantics(selected = selected, enabled = enabled, label = label)
                .clickable(
                    interactionSource = source,
                    indication = null,
                    enabled = enabled,
                    onClick = onSelect,
                ),
    ) {
        content()
    }
}

/**
 * Scope for [RadioGroupContent]: item rows bound to a shared [state].
 * Prefer [RadioGroup] for flat lists; use this only for custom layouts.
 */
class RadioGroupScope internal constructor(
    private val state: RadioGroupState,
) {
    @Composable
    fun <T> Item(
        item: RadioGroupValue<T>,
        enabled: Boolean = true,
        content: @Composable (selected: Boolean) -> Unit,
    ) {
        val selected = state.isSelected(item.value as Any?)
        RadioItem(
            selected = selected,
            enabled = enabled && state.enabled,
            label = item.label,
            onSelect = { state.select(item.value as Any?) },
        ) {
            content(selected)
        }
    }
}

/**
 * Custom-layout radio group: caller arranges [RadioGroupScope.Item] rows
 * inside [content]. Selection still lives in [state].
 */
@Composable
fun RadioGroupContent(
    state: RadioGroupState = rememberRadioGroupState(),
    label: String? = null,
    content: @Composable ColumnScope.(scope: RadioGroupScope) -> Unit,
) {
    val group = remember { FocusGroup() }
    FocusGroupEffect(group)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    CompositionLocalProvider(LocalFocusGroup provides group) {
        Column(modifier = Modifier.radioGroupSemantics(label).focusGroupKeys(group, rtl = rtl)) {
            content(RadioGroupScope(state))
        }
    }
}
