package com.biat.ui.primitives.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.biat.ui.core.accessibility.tabListSemantics
import com.biat.ui.core.accessibility.tabSemantics
import com.biat.ui.core.focus.FocusGroup
import com.biat.ui.core.focus.FocusGroupEffect
import com.biat.ui.core.focus.LocalFocusGroup
import com.biat.ui.core.focus.focusGroupItem
import com.biat.ui.core.focus.focusGroupKeys
import com.biat.ui.core.state.TabsState
import com.biat.ui.core.state.rememberTabsState

/**
 * Caller key for one tab. [value] drives [TabsState] selection;
 * [label] is announced on the tab for screen readers.
 */
data class TabValue<T>(
    val value: T,
    val label: String,
)

/** Keyboard axis of the tab list. Vertical lists use Up/Down instead of Left/Right. */
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
 * [layout] arranges the item slots without a library-defined Row or Column.
 *
 * Headless Tabs. Roving arrow-key navigation (orientation-aware, RTL-aware,
 * wrap-around) plus Home/End, selection state in [TabsState]. Zero styling.
 * [label] is exposed as the tab list content description for screen
 * readers; each tab already announces its own label + selected state.
 * Arrow keys move Compose focus between tabs; in [TabsActivation.Automatic]
 * (the default) they also select, in [TabsActivation.Manual] Enter/Space
 * (via click) or pointer click selects the focused tab.
 */
@Composable
fun <T> Tabs(
    state: TabsState = rememberTabsState(),
    tabs: List<TabValue<T>>,
    layout: @Composable (content: @Composable () -> Unit) -> Unit,
    label: String? = null,
    orientation: TabsOrientation = TabsOrientation.Horizontal,
    activation: TabsActivation = TabsActivation.Automatic,
    tab: @Composable (tab: TabValue<T>, selected: Boolean, onSelect: () -> Unit) -> Unit,
    panel: @Composable (selected: TabValue<T>?) -> Unit,
) {
    val group = remember { FocusGroup() }
    group.onMove = { index ->
        if (activation == TabsActivation.Automatic) tabs.getOrNull(index)?.let { state.select(it.value) }
    }
    FocusGroupEffect(group)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val listModifier =
        Modifier.tabListSemantics(label).focusGroupKeys(
            group,
            horizontal = orientation == TabsOrientation.Horizontal,
            rtl = rtl,
        )
    CompositionLocalProvider(LocalFocusGroup provides group) {
        Box(modifier = listModifier) {
            layout { tabs.forEach { item -> TabCell(state, item, tab) } }
        }
    }
    panel(tabs.firstOrNull { state.isSelected(it.value) })
}

@Composable
private fun <T> TabCell(
    state: TabsState,
    item: TabValue<T>,
    tab: @Composable (tab: TabValue<T>, selected: Boolean, onSelect: () -> Unit) -> Unit,
) {
    val selected = state.isSelected(item.value)
    val source = remember(item.value) { MutableInteractionSource() }
    Box(
        modifier =
            focusGroupItem(preferred = selected)
                .tabSemantics(selected = selected, label = item.label)
                .clickable(interactionSource = source, indication = null, onClick = { state.select(item.value) }),
    ) {
        tab(item, selected) { state.select(item.value) }
    }
}
