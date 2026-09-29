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

/**
 * Semantics for a headless checkbox. The announcement covers
 * checked/unchecked plus indeterminate.
 */
fun Modifier.checkboxSemantics(
    checked: Boolean,
    indeterminate: Boolean = false,
    enabled: Boolean = true,
    label: String? = null,
): Modifier = this.semantics(mergeDescendants = false) {
    role = Role.Checkbox
    this.selected = checked
    stateDescription = when {
        indeterminate -> "Indeterminate"
        checked -> "Checked"
        else -> "Unchecked"
    }
    if (!enabled) disabled()
    if (label != null) contentDescription = label
}

/**
 * Semantics for a headless switch. Announces on/off plus the caller label.
 */
fun Modifier.switchSemantics(
    checked: Boolean,
    enabled: Boolean = true,
    label: String? = null,
): Modifier = this.semantics(mergeDescendants = false) {
    role = Role.Switch
    this.selected = checked
    stateDescription = if (checked) "On" else "Off"
    if (!enabled) disabled()
    if (label != null) contentDescription = label
}

/** Semantics for radio group containers. */
fun Modifier.radioGroupSemantics(label: String? = null): Modifier =
    this.semantics(mergeDescendants = false) {
        if (label != null) contentDescription = label
    }

/**
 * Semantics for one radio option. Selection is the caller's [selected].
 */
fun Modifier.radioOptionSemantics(
    selected: Boolean,
    enabled: Boolean = true,
    label: String? = null,
): Modifier = this.semantics(mergeDescendants = false) {
    role = Role.RadioButton
    this.selected = selected
    if (!enabled) disabled()
    if (label != null) contentDescription = label
}
/**
 * Semantics for a headless toggle button. Announces pressed state.
 */
fun Modifier.toggleSemantics(
    pressed: Boolean,
    enabled: Boolean = true,
    label: String? = null,
): Modifier = this.semantics(mergeDescendants = false) {
    role = Role.Button
    this.selected = pressed
    stateDescription = if (pressed) "Pressed" else "Not pressed"
    if (!enabled) disabled()
    if (label != null) contentDescription = label
}

/**
 * Semantics for a headless slider. Announces value text plus caller label.
 */
fun Modifier.sliderSemantics(
    value: Float,
    valueText: String? = null,
    enabled: Boolean = true,
    label: String? = null,
): Modifier = this.semantics(mergeDescendants = false) {
    stateDescription = valueText ?: value.toString()
    if (!enabled) disabled()
    if (label != null) contentDescription = label
}

/** Semantics for a toolbar container. */
fun Modifier.toolbarSemantics(label: String? = null): Modifier =
    this.semantics(mergeDescendants = false) {
        if (label != null) contentDescription = label
    }

/** Semantics for one toolbar item. */
fun Modifier.toolbarItemSemantics(
    enabled: Boolean = true,
    label: String? = null,
): Modifier = this.semantics(mergeDescendants = false) {
    role = Role.Button
    if (!enabled) disabled()
    if (label != null) contentDescription = label
}

/** Semantics for a toggle-group container. */
fun Modifier.toggleGroupSemantics(label: String? = null): Modifier =
    this.semantics(mergeDescendants = false) {
        if (label != null) contentDescription = label
    }

/**
 * Semantics for a separator. Decorative separators carry no description;
 * semantic ones announce orientation.
 */
fun Modifier.separatorSemantics(
    decorative: Boolean = true,
    vertical: Boolean = false,
): Modifier = this.semantics(mergeDescendants = false) {
    stateDescription = if (decorative) {
        "Separator"
    } else if (vertical) {
        "Vertical separator"
    } else {
        "Horizontal separator"
    }
}

/** Semantics for a field label naming [controlLabel] for screen readers. */
fun Modifier.labelSemantics(controlLabel: String? = null): Modifier =
    this.semantics(mergeDescendants = false) {
        if (controlLabel != null) contentDescription = controlLabel
    }

/** Semantics for an avatar image with caller fallback text. */
fun Modifier.avatarSemantics(label: String? = null): Modifier =
    this.semantics(mergeDescendants = false) {
        role = Role.Image
        if (label != null) contentDescription = label
    }

/**
 * Semantics for a select trigger. Announces the dropdown role plus
 * expanded/collapsed state so screen readers know the listbox is open.
 */
fun Modifier.selectTriggerSemantics(expanded: Boolean, label: String? = null): Modifier =
    this.semantics(mergeDescendants = false) {
        role = Role.DropdownList
        stateDescription = if (expanded) "Expanded" else "Collapsed"
        if (label != null) contentDescription = label
    }

/**
 * Semantics for one select option. Announces button role with selection
 * plus highlighted state for the keyboard-navigated row.
 */
fun Modifier.selectOptionSemantics(
    selected: Boolean,
    highlighted: Boolean = false,
    label: String? = null,
): Modifier = this.semantics(mergeDescendants = false) {
    role = Role.Button
    this.selected = selected
    if (highlighted) stateDescription = "Highlighted"
    if (label != null) contentDescription = label
}

/**
 * Semantics for menu and popover triggers. Announces a button with the
 * caller label plus expanded state while the overlay is open.
 */
fun Modifier.overlayTriggerSemantics(expanded: Boolean, label: String? = null): Modifier =
    this.semantics(mergeDescendants = false) {
        role = Role.Button
        stateDescription = if (expanded) "Expanded" else "Collapsed"
        if (label != null) contentDescription = label
    }