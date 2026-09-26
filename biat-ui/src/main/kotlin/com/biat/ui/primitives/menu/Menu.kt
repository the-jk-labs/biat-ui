package com.biat.ui.primitives.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.biat.ui.core.accessibility.menuItemSemantics
import com.biat.ui.core.accessibility.menuSemantics
import com.biat.ui.core.state.MenuState
import com.biat.ui.core.state.rememberMenuState

/**
 * Headless dropdown Menu. Behavior only.
 *
 * Keyboard: Enter/Space on trigger opens; Up/Down cycle highlight;
 * Enter activates; Esc closes. Roving highlight lives in [MenuState].
 * Focus returns to the trigger whenever the menu closes.
 */
@Composable
fun Menu(
    state: MenuState = rememberMenuState(),
    dismissOnOutsideClick: Boolean = true,
    dismissOnEscape: Boolean = true,
    trigger: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val triggerSource = remember { MutableInteractionSource() }
    val triggerRequester = remember { FocusRequester() }

    // Restore focus to the trigger on open -> close transitions only,
    // regardless of how the menu was dismissed (Esc, item, outside tap).
    var wasOpen by remember { mutableStateOf(state.isOpen) }
    LaunchedEffect(state.isOpen) {
        if (wasOpen && !state.isOpen) {
            try {
                triggerRequester.requestFocus()
            } catch (_: IllegalStateException) {
                // Trigger not laid out; nothing to restore.
            }
        }
        wasOpen = state.isOpen
    }

    Box {
        Box(
            modifier = Modifier
                .focusRequester(triggerRequester)
                .clickable(
                    interactionSource = triggerSource,
                    indication = null,
                    onClick = { state.toggle() },
                )
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.Enter, Key.NumPadEnter, Key.Spacebar, Key.DirectionDown -> {
                            if (!state.isOpen) {
                                state.open()
                                true
                            } else false
                        }
                        else -> false
                    }
                },
        ) {
            trigger()
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
                    modifier = Modifier
                        .menuSemantics()
                        .onPreviewKeyEvent { event ->
                            if (event.type != KeyEventType.KeyDown) {
                                return@onPreviewKeyEvent false
                            }
                            when (event.key) {
                                Key.DirectionDown -> { state.moveHighlight(1); true }
                                Key.DirectionUp -> { state.moveHighlight(-1); true }
                                Key.MoveHome -> { state.highlight(0); true }
                                Key.MoveEnd -> { state.highlight(state.itemCount - 1); true }
                                Key.Escape -> {
                                    if (dismissOnEscape) {
                                        state.close()
                                        true
                                    } else false
                                }
                                else -> false
                            }
                        },
                    content = content,
                )
            }
        }
    }
}

/**
 * Headless menu item. Caller provides visuals; [selected] drives semantics.
 * Consumes click -> [onSelect] then closes via [state].
 */
@Composable
fun MenuItem(
    state: MenuState,
    onSelect: () -> Unit,
    selected: Boolean = false,
    label: String? = null,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .menuItemSemantics(label = label, selected = selected)
            .clickable(
                interactionSource = source,
                indication = null,
                onClick = {
                    onSelect()
                    state.close()
                },
            ),
    ) {
        content()
    }
}
