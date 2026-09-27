package com.biat.ui.primitives.radiogroup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.biat.ui.core.accessibility.radioGroupSemantics
import com.biat.ui.core.accessibility.radioOptionSemantics
import com.biat.ui.core.state.RadioGroupState
import com.biat.ui.core.state.rememberRadioGroupState

/**
 * Caller key for one radio option. [value] drives [RadioGroupState] selection;
 * [label] is announced on the option for screen readers.
 */
data class RadioValue<T>(val value: T, val label: String)

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
    options: List<RadioValue<T>>,
    label: String? = null,
    enabled: Boolean = true,
    disabledValues: Set<T> = emptySet(),
    option: @Composable (item: RadioValue<T>, selected: Boolean) -> Unit,
) {
    Column(modifier = Modifier.radioGroupSemantics(label)) {
        options.forEach { item ->
            RadioOption(
                selected = state.isSelected(item.value as Any?),
                enabled = enabled && item.value !in disabledValues,
                label = item.label,
                onSelect = { state.select(item.value as Any?) },
            ) {
                option(item, state.isSelected(item.value as Any?))
            }
        }
    }
}

/**
 * One radio row: selecting sets [selected] via [onSelect]. Compose directly
 * for custom layouts instead of [RadioGroup].
 */
@Composable
fun RadioOption(
    selected: Boolean,
    enabled: Boolean = true,
    label: String? = null,
    onSelect: () -> Unit,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .radioOptionSemantics(selected = selected, enabled = enabled, label = label)
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
 * Scope for [RadioGroupContent]: option rows bound to a shared [state].
 * Prefer [RadioGroup] for flat lists; use this only for custom layouts.
 */
class RadioGroupScope internal constructor(private val state: RadioGroupState) {
    @Composable
    fun <T> Option(
        item: RadioValue<T>,
        enabled: Boolean = true,
        content: @Composable (selected: Boolean) -> Unit,
    ) {
        val selected = state.isSelected(item.value as Any?)
        RadioOption(
            selected = selected,
            enabled = enabled,
            label = item.label,
            onSelect = { state.select(item.value as Any?) },
        ) {
            content(selected)
        }
    }
}

/**
 * Custom-layout radio group: caller arranges [RadioGroupScope.Option] rows
 * inside [content]. Selection still lives in [state].
 */
@Composable
fun RadioGroupContent(
    state: RadioGroupState = rememberRadioGroupState(),
    label: String? = null,
    content: @Composable ColumnScope.(scope: RadioGroupScope) -> Unit,
) {
    Column(modifier = Modifier.radioGroupSemantics(label)) {
        content(RadioGroupScope(state))
    }
}
