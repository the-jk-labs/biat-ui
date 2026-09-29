package com.biat.ui.primitives.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.biat.ui.core.accessibility.tabListSemantics
import com.biat.ui.core.accessibility.tabSemantics
import com.biat.ui.core.focus.safeRequestFocus
import com.biat.ui.core.state.TabMove
import com.biat.ui.core.state.TabsState
import com.biat.ui.core.state.rememberTabsState
import com.biat.ui.core.state.resolveTabIndex

data class TabValue<T>(val value: T, val label: String)

/** Layout axis of the tab list. Vertical lists use Up/Down instead of Left/Right. */
enum class TabsOrientation {
    Horizontal,
    Vertical,
}

/**
 * Selection behaviour. [Automatic] selects on arrow focus (WAI-APG automatic
 * activation). [Manual] moves focus only; Enter/Space or click activates.
 */
enum class TabsActivation {
    Automatic,
    Manual,
}

/**
 * Headless Tabs. Roving arrow-key navigation (orientation-aware, RTL-aware,
 * wrap-around) plus Home/End, selection state in [TabsState]. Zero styling.
 * [label] is exposed as the tab list content description for screen
 * readers; each tab already announces its own label + selected state.
 * Arrow keys move DOM focus between tabs; in [TabsActivation.Automatic]
 * (the default) they also select, in [TabsActivation.Manual] Enter/Space
 * (via click) or pointer click selects the focused tab.
 */
@Composable
fun <T> Tabs(
    state: TabsState = rememberTabsState(),
    tabs: List<TabValue<T>>,
    label: String? = null,
    orientation: TabsOrientation = TabsOrientation.Horizontal,
    activation: TabsActivation = TabsActivation.Automatic,
    tab: @Composable RowScope.(tab: TabValue<T>, selected: Boolean, onSelect: () -> Unit) -> Unit,
    panel: @Composable (selected: TabValue<T>?) -> Unit,
) {
    if (tabs.isEmpty()) {
        panel(null)
        return
    }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val requesters = remember(tabs.size) { List(tabs.size) { FocusRequester() } }
    var focusedIndex by remember(tabs.size) {
        mutableStateOf(
            tabs.indexOfFirst { state.isSelected(it.value) }.let { if (it < 0) 0 else it },
        )
    }

    fun moveFocus(next: Int) {
        focusedIndex = next
        requesters.getOrNull(next)?.safeRequestFocus()
        if (activation == TabsActivation.Automatic) {
            state.select(tabs[next].value as Any?)
        }
    }

    val listModifier = Modifier
        .tabListSemantics(label)
        .onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
            val move: TabMove = when (event.key) {
                Key.DirectionRight ->
                    if (orientation == TabsOrientation.Horizontal) {
                        if (isRtl) TabMove.Previous else TabMove.Next
                    } else {
                        return@onPreviewKeyEvent false
                    }
                Key.DirectionLeft ->
                    if (orientation == TabsOrientation.Horizontal) {
                        if (isRtl) TabMove.Next else TabMove.Previous
                    } else {
                        return@onPreviewKeyEvent false
                    }
                Key.DirectionUp ->
                    if (orientation == TabsOrientation.Vertical) {
                        TabMove.Previous
                    } else {
                        return@onPreviewKeyEvent false
                    }
                Key.DirectionDown ->
                    if (orientation == TabsOrientation.Vertical) {
                        TabMove.Next
                    } else {
                        return@onPreviewKeyEvent false
                    }
                Key.MoveHome -> TabMove.First
                Key.MoveEnd -> TabMove.Last
                else -> return@onPreviewKeyEvent false
            }
            moveFocus(resolveTabIndex(focusedIndex, tabs.size, move))
            true
        }
    if (orientation == TabsOrientation.Horizontal) {
        Row(modifier = listModifier) {
            tabs.forEachIndexed { index, item ->
                TabCell(requesters[index], state, item, tab) { focusedIndex = index }
            }
        }
    } else {
        Column(modifier = listModifier) {
            tabs.forEachIndexed { index, item ->
                TabCell(requesters[index], state, item, tab) { focusedIndex = index }
            }
        }
    }
    panel(tabs.firstOrNull { state.isSelected(it.value) })
}

@Composable
private fun <T> TabCell(
    requester: FocusRequester,
    state: TabsState,
    item: TabValue<T>,
    tab: @Composable RowScope.(tab: TabValue<T>, selected: Boolean, onSelect: () -> Unit) -> Unit,
    onFocused: () -> Unit,
) {
    val selected = state.isSelected(item.value)
    val source = remember(item.value) { MutableInteractionSource() }
    // Wrap caller tab UI with headless click + semantics. Clickable also
    // turns Enter/Space on the focused tab into activation for manual mode.
    Box(
        modifier = Modifier
            .focusRequester(requester)
            .tabSemantics(selected = selected, label = item.label)
            .clickable(
                interactionSource = source,
                indication = null,
                onClick = {
                    onFocused()
                    state.select(item.value as Any?)
                },
            ),
    ) {
        Row {
            tab(item, selected) {
                onFocused()
                state.select(item.value as Any?)
            }
        }
    }
}
