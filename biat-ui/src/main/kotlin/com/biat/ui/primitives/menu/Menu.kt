package com.biat.ui.primitives.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.biat.ui.core.accessibility.menuItemSemantics
import com.biat.ui.core.accessibility.menuSemantics
import com.biat.ui.core.accessibility.overlayTriggerSemantics
import com.biat.ui.core.focus.FocusReturnEffect
import com.biat.ui.core.focus.rememberFocusReturnRequester
import com.biat.ui.core.focus.safeRequestFocus
import com.biat.ui.core.positioning.PopupAlign
import com.biat.ui.core.positioning.PopupSide
import com.biat.ui.core.positioning.rememberBiatPopupPosition
import com.biat.ui.core.state.MenuState
import com.biat.ui.core.state.rememberMenuState

/**
 * Headless dropdown Menu. Behavior only.
 *
 * Keyboard: Enter/Space/Down on trigger opens; Up/Down cycle highlight,
 * Home/End jump; Esc closes. Roving highlight lives in [MenuState].
 * Activating the highlighted entry is caller-owned: focus a [MenuItem]
 * (clickable, so Enter/Space fires its [onSelect]) or observe
 * [MenuState.highlightedIndex]. Unhandled Enter inside the menu falls
 * through to the caller.
 * Placement follows [side]/[align] with [sideOffset]/[alignOffset] gaps;
 * [avoidCollisions] flips to the opposite side when it overflows less and
 * shifts the menu to stay on-screen.
 * [label] is exposed as the menu content description for screen readers
 * and names the trigger while collapsed/expanded.
 * Focus returns to the trigger whenever the menu closes.
 */
@Composable
fun Menu(
    state: MenuState = rememberMenuState(),
    dismissOnOutsideClick: Boolean = true,
    dismissOnEscape: Boolean = true,
    label: String? = null,
    side: PopupSide = PopupSide.Bottom,
    align: PopupAlign = PopupAlign.Start,
    sideOffset: Dp = 0.dp,
    alignOffset: Dp = 0.dp,
    avoidCollisions: Boolean = true,
    trigger: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val triggerSource = remember { MutableInteractionSource() }
    val triggerRequester = rememberFocusReturnRequester()

    // Restore focus to the trigger on open -> close transitions only,
    // regardless of how the menu was dismissed (Esc, item, outside tap).
    FocusReturnEffect(isOpen = state.isOpen, returnRequester = triggerRequester)

    Box {
        Box(
            modifier = Modifier
                .focusRequester(triggerRequester)
                .overlayTriggerSemantics(expanded = state.isOpen, label = label)
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
                popupPositionProvider = rememberBiatPopupPosition(
                    side = side,
                    align = align,
                    sideOffset = sideOffset,
                    alignOffset = alignOffset,
                    avoidCollisions = avoidCollisions,
                ),
                onDismissRequest = { state.close() },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = dismissOnOutsideClick,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .menuSemantics(label)
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
 * When [enabled] is false the item is not clickable, announces disabled,
 * and must sit at an index in [MenuState.disabledIndices] so keyboard
 * highlight skips it.
 */
@Composable
fun MenuItem(
    state: MenuState,
    onSelect: () -> Unit,
    selected: Boolean = false,
    enabled: Boolean = true,
    label: String? = null,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val semantics = Modifier.menuItemSemantics(
        label = label,
        selected = selected,
        enabled = enabled,
    )
    if (enabled) {
        Box(
            modifier = semantics
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
    } else {
        Box(modifier = semantics) {
            content()
        }
    }
}

/**
 * Headless menu separator. Pure visual boundary owned by the caller;
 * never focusable, clickable, or counted in [MenuState.itemCount].
 */
@Composable
fun MenuSeparator(
    content: @Composable () -> Unit,
) {
    Box {
        content()
    }
}

/**
 * Headless checkbox menu item. Toggleable behavior with checkbox semantics;
 * stays open after toggling so callers can flip several options in a row.
 * Counts as one highlightable entry in [MenuState.itemCount].
 */
@Composable
fun MenuCheckboxItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    label: String? = null,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .semantics(mergeDescendants = false) {
                if (label != null) contentDescription = label
            }
            .toggleable(
                value = checked,
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            ),
    ) {
        content()
    }
}

/**
 * Headless radio menu item. Selectable behavior with radio semantics;
 * closes the menu after choosing, like [MenuItem]. Sibling items share
 * one caller-owned value; each counts as one entry in [MenuState.itemCount].
 */
@Composable
fun MenuRadioItem(
    state: MenuState,
    selected: Boolean,
    onSelect: () -> Unit,
    enabled: Boolean = true,
    label: String? = null,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .semantics(mergeDescendants = false) {
                if (label != null) contentDescription = label
            }
            .selectable(
                selected = selected,
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = {
                    onSelect()
                    state.close()
                },
            ),
    ) {
        content()
    }
}

/**
 * Headless submenu: a trigger item opening a nested menu anchored to the
 * trigger's trailing edge ([side]) and top ([align]).
 *
 * Click or ArrowRight on the trigger opens; Esc or ArrowLeft inside closes
 * only this submenu and returns focus to its trigger. The parent menu stays
 * open underneath in both cases. Counts as one highlightable entry in the
 * parent [MenuState.itemCount]; its own [state] needs its own count.
 */
@Composable
fun MenuSub(
    state: MenuState = rememberMenuState(),
    label: String? = null,
    dismissOnOutsideClick: Boolean = true,
    dismissOnEscape: Boolean = true,
    side: PopupSide = PopupSide.End,
    align: PopupAlign = PopupAlign.Start,
    sideOffset: Dp = 0.dp,
    alignOffset: Dp = 0.dp,
    avoidCollisions: Boolean = true,
    trigger: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val triggerRequester = rememberFocusReturnRequester()
    val subFocus = remember { FocusRequester() }
    FocusReturnEffect(isOpen = state.isOpen, returnRequester = triggerRequester)

    Box {
        val source = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .focusRequester(triggerRequester)
                .menuItemSemantics(label = label)
                .clickable(
                    interactionSource = source,
                    indication = null,
                    onClick = { state.open() },
                )
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    if (event.key == Key.DirectionRight && !state.isOpen) {
                        state.open()
                        true
                    } else {
                        false
                    }
                },
        ) {
            trigger()
        }

        if (state.isOpen) {
            LaunchedEffect(Unit) { subFocus.safeRequestFocus() }
            Popup(
                popupPositionProvider = rememberBiatPopupPosition(
                    side = side,
                    align = align,
                    sideOffset = sideOffset,
                    alignOffset = alignOffset,
                    avoidCollisions = avoidCollisions,
                ),
                onDismissRequest = { state.close() },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = dismissOnOutsideClick,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .menuSemantics(label)
                        .focusRequester(subFocus)
                        .focusable()
                        .onPreviewKeyEvent { event ->
                            if (event.type != KeyEventType.KeyDown) {
                                return@onPreviewKeyEvent false
                            }
                            when (event.key) {
                                Key.DirectionDown -> { state.moveHighlight(1); true }
                                Key.DirectionUp -> { state.moveHighlight(-1); true }
                                Key.MoveHome -> { state.highlight(0); true }
                                Key.MoveEnd -> { state.highlight(state.itemCount - 1); true }
                                Key.Escape, Key.DirectionLeft -> {
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
