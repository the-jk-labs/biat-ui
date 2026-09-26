package com.biat.ui.primitives.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.biat.ui.core.accessibility.tabListSemantics
import com.biat.ui.core.accessibility.tabSemantics
import com.biat.ui.core.state.TabsState
import com.biat.ui.core.state.rememberTabsState

data class TabValue<T>(val value: T, val label: String)

/**
 * Headless Tabs. Roving arrow-key navigation (Left/Right + Home/End,
 * RTL-aware), selection state in [TabsState]. Zero styling.
 */
@Composable
fun <T> Tabs(
    state: TabsState = rememberTabsState(),
    tabs: List<TabValue<T>>,
    tab: @Composable RowScope.(tab: TabValue<T>, selected: Boolean, onSelect: () -> Unit) -> Unit,
    panel: @Composable (selected: TabValue<T>?) -> Unit,
) {
    if (tabs.isEmpty()) {
        panel(null)
        return
    }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Row(
        modifier = Modifier
            .tabListSemantics()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                val current = tabs.indexOfFirst { state.isSelected(it.value) }.let {
                    if (it < 0) 0 else it
                }
                when (event.key) {
                    Key.DirectionRight -> {
                        val next = (current + if (isRtl) -1 else 1).mod(tabs.size)
                        state.select(tabs[next].value as Any?)
                        true
                    }
                    Key.DirectionLeft -> {
                        val next = (current + if (isRtl) 1 else -1).mod(tabs.size)
                        state.select(tabs[next].value as Any?)
                        true
                    }
                    Key.MoveHome -> {
                        state.select(tabs.first().value as Any?)
                        true
                    }
                    Key.MoveEnd -> {
                        state.select(tabs.last().value as Any?)
                        true
                    }
                    else -> false
                }
            },
    ) {
        tabs.forEach { item ->
            val selected = state.isSelected(item.value)
            val source = remember(item.value) { MutableInteractionSource() }
            // Wrap caller tab UI with headless click + semantics.
            Box(
                modifier = Modifier
                    .tabSemantics(selected = selected, label = item.label)
                    .clickable(
                        interactionSource = source,
                        indication = null,
                        onClick = { state.select(item.value as Any?) },
                    ),
            ) {
                Row {
                    tab(item, selected) { state.select(item.value as Any?) }
                }
            }
        }
    }
    panel(tabs.firstOrNull { state.isSelected(it.value) })
}
