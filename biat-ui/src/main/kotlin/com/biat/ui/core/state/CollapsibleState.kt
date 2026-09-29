package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Logic-only state for Collapsible. No UI, no styling.
 *
 * Openness contract (controlled vs uncontrolled):
 * - Uncontrolled (default): pass [initialExpanded]; the state owns the value and
 *   [expand]/[collapse]/[toggle]/[setExpanded] mutate it, notifying [onExpandedChange].
 * - Controlled: pass non-null [controlledExpanded] plus [onExpandedChange]; the caller
 *   owns the value and mutators only notify via [onExpandedChange] without mutating.
 *   The caller must reflect the new value back into [controlledExpanded]
 *   ([rememberCollapsibleState] does this every recomposition). Do not switch
 *   modes during the state's lifetime.
 */
@Stable
class CollapsibleState(
    initialExpanded: Boolean = false,
    controlledExpanded: Boolean? = null,
    var onExpandedChange: ((Boolean) -> Unit)? = null,
) {
    private var internalExpanded by mutableStateOf(initialExpanded)

    /**
     * Caller-owned value in controlled mode. Managed by [rememberCollapsibleState];
     * assign only to reflect the caller's value.
     */
    var controlledExpanded: Boolean? by mutableStateOf(controlledExpanded)

    val isExpanded: Boolean get() = controlledExpanded ?: internalExpanded

    fun expand() = setExpanded(true)

    fun collapse() = setExpanded(false)

    fun toggle() = setExpanded(!isExpanded)

    fun setExpanded(expanded: Boolean) {
        if (isExpanded == expanded) return
        if (controlledExpanded != null) {
            onExpandedChange?.invoke(expanded)
        } else {
            internalExpanded = expanded
            onExpandedChange?.invoke(expanded)
        }
    }
}

/**
 * Remembers [CollapsibleState] across recompositions. The instance survives;
 * [controlledExpanded] and [onExpandedChange] are refreshed every
 * recomposition. In controlled mode changes only notify.
 */
@Composable
fun rememberCollapsibleState(
    initialExpanded: Boolean = false,
    controlledExpanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
): CollapsibleState =
    remember {
        CollapsibleState(initialExpanded = initialExpanded)
    }.apply {
        this.controlledExpanded = controlledExpanded
        this.onExpandedChange = onExpandedChange
    }
