package com.biat.ui.core.accessibility

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

/** Semantics for overlay dialogs. Headless: label comes from caller. */
fun Modifier.dialogSemantics(label: String? = null): Modifier =
    this.semantics(mergeDescendants = false) {
        if (label != null) contentDescription = label
    }

/** Semantics for menu containers. */
fun Modifier.menuSemantics(label: String? = null): Modifier =
    this.semantics(mergeDescendants = false) {
        if (label != null) contentDescription = label
    }

/** Semantics for a menu item with selection state. */
fun Modifier.menuItemSemantics(
    label: String? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
): Modifier = this.semantics(mergeDescendants = false) {
    role = Role.Button
    this.selected = selected
    if (!enabled) disabled()
    if (label != null) contentDescription = label
}

/** Semantics for tabs. */
fun Modifier.tabSemantics(selected: Boolean, label: String? = null): Modifier =
    this.semantics(mergeDescendants = false) {
        role = Role.Tab
        this.selected = selected
        if (label != null) contentDescription = label
    }

fun Modifier.tabListSemantics(label: String? = null): Modifier =
    this.semantics(mergeDescendants = false) {
        if (label != null) contentDescription = label
    }

/** Semantics for tooltip anchors. */
fun Modifier.tooltipAnchorSemantics(tip: String): Modifier =
    this.semantics(mergeDescendants = false) {
        contentDescription = tip
        stateDescription = tip
    }

/**
 * Semantics for collapsible triggers. Announces the trigger label plus
 * expanded state for screen readers.
 */
fun Modifier.collapsibleTriggerSemantics(expanded: Boolean, label: String? = null): Modifier =
    this.semantics(mergeDescendants = false) {
        role = Role.Button
        this.selected = expanded
        stateDescription = if (expanded) "Expanded" else "Collapsed"
        if (label != null) contentDescription = label
    }

/**
 * Semantics for accordion item triggers. [type] names the widget
 * ("Accordion item" by default) so grouped items stay distinct.
 */
fun Modifier.accordionTriggerSemantics(
    expanded: Boolean,
    label: String? = null,
    type: String = "Accordion item",
): Modifier = this.semantics(mergeDescendants = false) {
    role = Role.Button
    this.selected = expanded
    stateDescription = if (expanded) "Expanded" else "Collapsed"
    contentDescription = listOfNotNull(type, label).joinToString(": ")
}