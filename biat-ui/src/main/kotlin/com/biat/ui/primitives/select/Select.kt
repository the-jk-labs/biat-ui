package com.biat.ui.primitives.select

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.biat.ui.core.accessibility.selectItemSemantics
import com.biat.ui.core.accessibility.selectTriggerSemantics
import com.biat.ui.core.focus.FocusReturnEffect
import com.biat.ui.core.focus.rememberFocusReturnRequester
import com.biat.ui.core.positioning.PopupAlign
import com.biat.ui.core.positioning.PopupSide
import com.biat.ui.core.positioning.rememberBiatPopupPosition
import com.biat.ui.core.state.SelectState
import com.biat.ui.core.state.rememberSelectState

/**
 * Headless Select / Combobox trigger + listbox.
 *
 * [options] drive keyboard highlight; [onSelected] fires on
 * commit. Caller owns trigger + option row visuals entirely.
 * Listbox placement follows [side]/[align] with [sideOffset]/[alignOffset]
 * gaps; [avoidCollisions] flips to the opposite side when it overflows
 * less and shifts the listbox to stay on-screen.
 * Focus returns to the trigger whenever the listbox closes.
 * [label] names the trigger for screen readers; each option announces
 * its selection and highlight state.
 *
 * Keyboard: Enter/Space/Down on a closed trigger opens; Up/Down move
 * highlight (Up from none lands on the last option), Home/End jump;
 * Enter commits the highlighted option and falls through when nothing is
 * highlighted; Esc closes.
 *
 * Combobox filtering: bind a text field inside [trigger] to
 * [SelectState.query] via [SelectState.setQuery]; the listbox renders
 * [SelectState.filteredOptions] using [queryToString]. Async options work
 * by passing a new [options] list as data arrives; highlight clamps to
 * the visible range and [isLoading] swaps the list for [loading].
 * When the filtered list is empty the [empty] slot renders instead.
 * Clearable: Backspace/Delete on a closed trigger with an empty query
 * clears the selection via [SelectState.clearSelection]. Single-key
 * typeahead moves highlight to the next visible option starting with
 * the typed character when no text field consumes it.
 */
@Composable
fun <T> Select(
    state: SelectState<T> = rememberSelectState(),
    options: List<T>,
    dismissOnOutsideClick: Boolean = true,
    dismissOnEscape: Boolean = true,
    label: String? = null,
    side: PopupSide = PopupSide.Bottom,
    align: PopupAlign = PopupAlign.Start,
    sideOffset: Dp = 0.dp,
    alignOffset: Dp = 0.dp,
    avoidCollisions: Boolean = true,
    queryToString: (T) -> String = { it.toString() },
    isLoading: Boolean = false,
    loading: @Composable () -> Unit = {},
    empty: @Composable () -> Unit = {},
    trigger: @Composable (selected: T?) -> Unit,
    item: @Composable ColumnScope.(value: T, highlighted: Boolean, selected: Boolean) -> Unit,
    onSelected: ((T?) -> Unit)? = null,
) {
    val visible = state.filteredOptions(options, queryToString)
    LaunchedEffect(visible.size, state.isOpen) {
        if (state.highlightedIndex >= visible.size) {
            state.highlightedIndex = if (visible.isEmpty()) -1 else visible.size - 1
        }
    }
    Box {
        val source = remember { MutableInteractionSource() }
        val returnRequester = rememberFocusReturnRequester()
        FocusReturnEffect(isOpen = state.isOpen, returnRequester = returnRequester)
        Box(
            modifier =
                Modifier
                    .focusRequester(returnRequester)
                    .selectTriggerSemantics(expanded = state.isOpen, label = label)
                    .clickable(
                        interactionSource = source,
                        indication = null,
                        onClick = { state.toggle() },
                    ).onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                        when (event.key) {
                            Key.Enter, Key.NumPadEnter, Key.Spacebar, Key.DirectionDown -> {
                                if (!state.isOpen) {
                                    state.open()
                                    true
                                } else {
                                    false
                                }
                            }

                            Key.Backspace, Key.Delete -> {
                                if (!state.isOpen && state.query.isEmpty() &&
                                    state.selected != null
                                ) {
                                    state.clearSelection()
                                    onSelected?.invoke(null)
                                    true
                                } else {
                                    false
                                }
                            }

                            else -> {
                                false
                            }
                        }
                    },
        ) {
            trigger(state.selected)
        }

        if (state.isOpen) {
            Popup(
                popupPositionProvider =
                    rememberBiatPopupPosition(
                        side = side,
                        align = align,
                        sideOffset = sideOffset,
                        alignOffset = alignOffset,
                        avoidCollisions = avoidCollisions,
                    ),
                onDismissRequest = { state.close() },
                properties =
                    PopupProperties(
                        focusable = true,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = dismissOnOutsideClick,
                    ),
            ) {
                Column(
                    modifier =
                        Modifier.onPreviewKeyEvent { event ->
                            if (event.type != KeyEventType.KeyDown) {
                                return@onPreviewKeyEvent false
                            }
                            when (event.key) {
                                Key.DirectionDown -> {
                                    if (visible.isEmpty()) return@onPreviewKeyEvent false
                                    state.moveHighlight(1, visible.size)
                                    true
                                }

                                Key.DirectionUp -> {
                                    if (visible.isEmpty()) return@onPreviewKeyEvent false
                                    state.moveHighlight(-1, visible.size)
                                    true
                                }

                                Key.MoveHome -> {
                                    if (visible.isEmpty()) return@onPreviewKeyEvent false
                                    state.highlightFirst(visible.size)
                                    true
                                }

                                Key.MoveEnd -> {
                                    if (visible.isEmpty()) return@onPreviewKeyEvent false
                                    state.highlightLast(visible.size)
                                    true
                                }

                                Key.Enter, Key.NumPadEnter -> {
                                    val value = state.commitValue(visible)
                                    if (value != null) {
                                        state.select(value)
                                        onSelected?.invoke(value)
                                        true
                                    } else {
                                        false
                                    }
                                }

                                Key.Escape -> {
                                    if (dismissOnEscape) {
                                        state.close()
                                        true
                                    } else {
                                        false
                                    }
                                }

                                else -> {
                                    val code = event.utf16CodePoint
                                    if (code in 32..126 && !event.isCtrlPressed &&
                                        !event.isMetaPressed
                                    ) {
                                        state.moveHighlightToMatch(
                                            visible,
                                            queryToString,
                                            code.toChar().toString(),
                                        )
                                    } else {
                                        false
                                    }
                                }
                            }
                        },
                ) {
                    when {
                        isLoading -> {
                            loading()
                        }

                        visible.isEmpty() -> {
                            empty()
                        }

                        else -> {
                            visible.forEachIndexed { index, value ->
                                val itemSource = remember { MutableInteractionSource() }
                                Box(
                                    modifier =
                                        Modifier
                                            .selectItemSemantics(
                                                selected = state.selected == value,
                                                highlighted = state.highlightedIndex == index,
                                            ).clickable(
                                                interactionSource = itemSource,
                                                indication = null,
                                                onClick = {
                                                    state.select(value)
                                                    onSelected?.invoke(value)
                                                },
                                            ),
                                ) {
                                    Column {
                                        item(
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
    }
}
