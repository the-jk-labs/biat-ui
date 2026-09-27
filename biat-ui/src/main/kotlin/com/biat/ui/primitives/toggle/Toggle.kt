package com.biat.ui.primitives.toggle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.biat.ui.core.accessibility.checkboxSemantics
import com.biat.ui.core.accessibility.switchSemantics
import com.biat.ui.core.accessibility.toggleSemantics
import com.biat.ui.core.state.ToggleState
import com.biat.ui.core.state.ToggleValue
import com.biat.ui.core.state.rememberToggleState

/**
 * Shared headless click + semantics wrapper for Checkbox, Switch, and
 * ToggleButton. The caller owns every pixel inside [content].
 */
@Composable
private fun ToggleBox(
    onToggle: () -> Unit,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier = modifier.clickable(
            interactionSource = source,
            indication = null,
            onClick = onToggle,
        ),
    ) {
        content()
    }
}

/**
 * Headless Checkbox. Behavior only, zero styling.
 *
 * Toggle via click; [ToggleValue.Indeterminate] activates to On. [label] is
 * exposed as the content description for screen readers. Tri-state comes
 * from the state value, not a separate flag.
 */
@Composable
fun Checkbox(
    state: ToggleState = rememberToggleState(),
    enabled: Boolean = true,
    label: String? = null,
    content: @Composable (value: ToggleValue) -> Unit,
) {
    ToggleBox(
        onToggle = { if (enabled) state.toggle() },
        modifier = Modifier.checkboxSemantics(
            checked = state.isOn,
            indeterminate = state.isIndeterminate,
            enabled = enabled,
            label = label,
        ),
    ) {
        content(state.value)
    }
}

/**
 * Headless Switch. Same behavior contract as [Checkbox] with switch
 * semantics (on/off). Indeterminate still activates to On.
 */
@Composable
fun Switch(
    state: ToggleState = rememberToggleState(),
    enabled: Boolean = true,
    label: String? = null,
    content: @Composable (checked: Boolean) -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .switchSemantics(checked = state.isOn, enabled = enabled, label = label)
            .toggleable(
                value = state.isOn,
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = { state.setValue(if (it) ToggleValue.On else ToggleValue.Off) },
            ),
    ) {
        content(state.isOn)
    }
}

/**
 * Headless ToggleButton (pressed on/off button). Behavior only, zero styling.
 *
 * Click toggles pressed state; indeterminate (when shared with tri-state
 * callers) activates to pressed. [label] names the button for screen readers.
 * For grouped toggles see `ToggleGroup`.
 */
@Composable
fun ToggleButton(
    state: ToggleState = rememberToggleState(),
    enabled: Boolean = true,
    label: String? = null,
    content: @Composable (pressed: Boolean) -> Unit,
) {
    ToggleBox(
        onToggle = { if (enabled) state.toggle() },
        modifier = Modifier.toggleSemantics(
            pressed = state.isOn,
            enabled = enabled,
            label = label,
        ),
    ) {
        content(state.isOn)
    }
}
