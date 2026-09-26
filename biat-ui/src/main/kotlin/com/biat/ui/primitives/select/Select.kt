package com.biat.ui.primitives.select

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.biat.ui.core.state.SelectState
import com.biat.ui.core.state.rememberSelectState

/**
 * Headless Select / Combobox trigger + listbox.
 *
 * [options] drive keyboard highlight; [onSelected] fires on
 * commit. Caller owns trigger + option row visuals entirely.
 */
@Composable
fun <T> Select(
    state: SelectState<T> = rememberSelectState(),
    options: List<T>,
    dismissOnOutsideClick: Boolean = true,
    dismissOnEscape: Boolean = true,
    trigger: @Composable (selected: T?) -> Unit,
    option: @Composable ColumnScope.(value: T, highlighted: Boolean, selected: Boolean) -> Unit,
    onSelected: ((T?) -> Unit)? = null,
) {
    Box {
        val source = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .semantics(mergeDescendants = false) { role = Role.DropdownList }
                .clickable(
                    interactionSource = source,
                    indication = null,
                    onClick = { state.toggle() },
                )
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.Enter, Key.NumPadEnter, Key.Spacebar, Key.DirectionDown -> {
                            if (!state.isOpen) { state.open(); true } else false
                        }
                        else -> false
                    }
                },
        ) {
            trigger(state.selected)
        }

        if (state.isOpen) {
            Popup(
                onDismissRequest = { state.close() },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = dismissOnOutsideClick,
                ),
            ) {
                Column(
                    modifier = Modifier.onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) {
                            return@onPreviewKeyEvent false
                        }
                        when (event.key) {
                            Key.DirectionDown -> {
                                state.highlightedIndex =
                                    ((state.highlightedIndex + 1).mod(options.size))
                                true
                            }
                            Key.DirectionUp -> {
                                state.highlightedIndex =
                                    ((state.highlightedIndex - 1).mod(options.size))
                                true
                            }
                            Key.Enter, Key.NumPadEnter -> {
                                val value = options.getOrNull(state.highlightedIndex)
                                if (value != null) {
                                    state.select(value)
                                    onSelected?.invoke(value)
                                }
                                true
                            }
                            Key.Escape -> {
                                if (dismissOnEscape) { state.close(); true } else false
                            }
                            else -> false
                        }
                    },
                ) {
                    options.forEachIndexed { index, value ->
                        val itemSource = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier.clickable(
                                interactionSource = itemSource,
                                indication = null,
                                onClick = {
                                    state.select(value)
                                    onSelected?.invoke(value)
                                },
                            ),
                        ) {
                            Column {
                                option(
                                    value,
                                    state.highlightedIndex == index,
                                    state.selected == value,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
