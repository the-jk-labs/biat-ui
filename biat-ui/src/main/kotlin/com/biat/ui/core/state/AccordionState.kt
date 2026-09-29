package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Which values an Accordion keeps open.
 *
 * - [Single]: at most one item open. Selecting the open item closes it when
 *   [collapsible] is true, otherwise it stays open.
 * - [Multiple]: any subset open. Selecting toggles membership.
 */
enum class AccordionType { Single, Multiple }

/**
 * Logic-only state for Accordion. No UI, no styling.
 *
 * Open values are caller keys ([Any]). Single mode holds zero or one value;
 * Multiple mode holds a set. [openValues] is the current snapshot.
 * Every change is mirrored to [onOpenChange]. Expansion itself stays
 * uncontrolled; the caller renders panels from [isOpen].
 *
 * - [collapsible]: Single mode only. When false the open item cannot be
 *   closed by selecting it (at least one stays open).
 */
@Stable
class AccordionState(
    initialValues: List<Any?> = emptyList(),
    val type: AccordionType = AccordionType.Single,
    val collapsible: Boolean = true,
    var onOpenChange: ((List<Any?>) -> Unit)? = null,
) {
    var openValues: List<Any?> by mutableStateOf(
        if (type == AccordionType.Single) initialValues.take(1) else initialValues.toList(),
    )
        private set

    fun isOpen(value: Any?): Boolean = openValues.contains(value)

    /**
     * Selects [value]: opens, closes, or toggles per [type] and [collapsible].
     * No-op duplicates still notify nothing; only real changes notify.
     */
    fun select(value: Any?) {
        if (type == AccordionType.Single) {
            if (isOpen(value)) {
                if (collapsible) {
                    openValues = emptyList()
                    onOpenChange?.invoke(openValues)
                }
            } else {
                openValues = listOf(value)
                onOpenChange?.invoke(openValues)
            }
        } else {
            openValues = if (isOpen(value)) {
                openValues.filterNot { it == value }
            } else {
                openValues + value
            }
            onOpenChange?.invoke(openValues)
        }
    }

    fun open(value: Any?) {
        if (isOpen(value)) return
        openValues = if (type == AccordionType.Single) listOf(value) else openValues + value
        onOpenChange?.invoke(openValues)
    }

    fun close(value: Any?) {
        if (!isOpen(value)) return
        if (type == AccordionType.Single && !collapsible && openValues.size <= 1) return
        openValues = openValues.filterNot { it == value }
        onOpenChange?.invoke(openValues)
    }
}

@Composable
fun rememberAccordionState(
    initialValues: List<Any?> = emptyList(),
    type: AccordionType = AccordionType.Single,
    collapsible: Boolean = true,
    onOpenChange: ((List<Any?>) -> Unit)? = null,
): AccordionState = remember {
    AccordionState(
        initialValues = initialValues,
        type = type,
        collapsible = collapsible,
    )
}.apply {
    this.onOpenChange = onOpenChange
}
