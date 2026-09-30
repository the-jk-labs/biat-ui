package com.biat.ui.primitives.accordion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.biat.ui.core.accessibility.accordionTriggerSemantics
import com.biat.ui.core.state.AccordionState
import com.biat.ui.core.state.AccordionType
import com.biat.ui.core.state.rememberAccordionState

/**
 * Caller key for one accordion item. [value] drives [AccordionState] open
 * membership; [label] is announced on the trigger for screen readers.
 */
data class AccordionValue<T>(
    val value: T,
    val label: String,
)

/**
 * Headless Accordion. Behavior only, zero styling. [layout] arranges the caller slots.
 *
 * Renders [items] inline; each trigger selects its value in [state] and its
 * panel renders when open. [AccordionState.type] controls expansion and
 * [AccordionState.collapsible] controls closing. [itemLayout] arranges each
 * trigger and panel; by default it reuses the caller's [layout]. Trigger clicks and
 * panels are caller visuals entirely.
 */
@Composable
fun <T> Accordion(
    state: AccordionState = rememberAccordionState(),
    items: List<AccordionValue<T>>,
    layout: @Composable (content: @Composable () -> Unit) -> Unit,
    itemLayout: @Composable (content: @Composable () -> Unit) -> Unit = layout,
    trigger: @Composable (item: AccordionValue<T>, expanded: Boolean) -> Unit,
    content: @Composable (item: AccordionValue<T>) -> Unit,
) {
    Box {
        layout {
            items.forEach { item ->
                AccordionItem(state = state, item = item, layout = itemLayout, trigger = trigger, content = content)
            }
        }
    }
}

/**
 * One accordion item: trigger toggles [item] in [state]; [layout] arranges
 * the trigger and its panel when open. Compose directly for custom layouts instead of [Accordion].
 */
@Composable
fun <T> AccordionItem(
    state: AccordionState,
    item: AccordionValue<T>,
    layout: @Composable (content: @Composable () -> Unit) -> Unit,
    trigger: @Composable (item: AccordionValue<T>, expanded: Boolean) -> Unit,
    content: @Composable (item: AccordionValue<T>) -> Unit,
) {
    val expanded = state.isOpen(item.value as Any?)
    Box {
        layout {
            val source = remember(item.value) { MutableInteractionSource() }
            Box(
                modifier =
                    Modifier
                        .accordionTriggerSemantics(expanded = expanded, label = item.label)
                        .clickable(
                            interactionSource = source,
                            indication = null,
                            onClick = { state.select(item.value as Any?) },
                        ),
            ) {
                trigger(item, expanded)
            }
            if (expanded) {
                content(item)
            }
        }
    }
}

/**
 * Convenience overload defaulting to single collapsible state. Kept for
 * callers that only need one open section without owning a state.
 */
@Composable
fun <T> Accordion(
    items: List<AccordionValue<T>>,
    layout: @Composable (content: @Composable () -> Unit) -> Unit,
    itemLayout: @Composable (content: @Composable () -> Unit) -> Unit = layout,
    type: AccordionType = AccordionType.Single,
    collapsible: Boolean = true,
    trigger: @Composable (item: AccordionValue<T>, expanded: Boolean) -> Unit,
    content: @Composable (item: AccordionValue<T>) -> Unit,
) {
    val state = rememberAccordionState(type = type, collapsible = collapsible)
    Accordion(state = state, items = items, layout = layout, itemLayout = itemLayout, trigger = trigger, content = content)
}
