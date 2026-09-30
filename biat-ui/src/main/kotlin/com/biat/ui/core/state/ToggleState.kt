package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Tri-state for Checkbox: [Off], [On], or [Indeterminate] (partial).
 * [ToggleState.toggle] maps Indeterminate to On, matching platform convention.
 */
enum class ToggleValue { Off, On, Indeterminate }

/** True when the receiver is [ToggleValue.On]. */
fun ToggleValue.isOn(): Boolean = this == ToggleValue.On

/**
 * Next value for a headless toggle. Indeterminate activates to On;
 * On and Off flip each other.
 */
fun ToggleValue.next(): ToggleValue =
    when (this) {
        ToggleValue.Off -> ToggleValue.On
        ToggleValue.On -> ToggleValue.Off
        ToggleValue.Indeterminate -> ToggleValue.On
    }

/**
 * Logic-only state for a headless toggle (Checkbox / Switch).
 * No UI, no styling.
 *
 * Checked contract (controlled vs uncontrolled):
 * - Uncontrolled (default): pass [initialValue]; the state owns the value and
 *   [toggle]/[setValue] mutate it, notifying [onValueChange].
 * - Controlled: pass non-null [controlledValue] plus [onValueChange]; the caller
 *   owns the value and mutators only notify without mutating. The caller must
 *   reflect the new value back into [controlledValue] ([rememberToggleState]
 *   does this every recomposition). Do not switch modes during the state's lifetime.
 * - When [enabled] is false mutators are ignored entirely (no notify).
 */
@Stable
class ToggleState(
    initialValue: ToggleValue = ToggleValue.Off,
    controlledValue: ToggleValue? = null,
    var enabled: Boolean = true,
    var onValueChange: ((ToggleValue) -> Unit)? = null,
) {
    private var internalValue by mutableStateOf(initialValue)

    /**
     * Caller-owned value in controlled mode. Managed by [rememberToggleState];
     * assign only to reflect the caller's value.
     */
    var controlledValue: ToggleValue? by mutableStateOf(controlledValue)

    val value: ToggleValue get() = controlledValue ?: internalValue

    /** Convenience: true when [value] is [ToggleValue.On]. */
    val isOn: Boolean get() = value.isOn()

    /** True when [value] is [ToggleValue.Indeterminate]. */
    val isIndeterminate: Boolean get() = value == ToggleValue.Indeterminate

    fun toggle() = setValue(value.next())

    @JvmName("setToggleValueState")
    fun setValue(next: ToggleValue) {
        if (!enabled || value == next) return
        if (controlledValue != null) {
            onValueChange?.invoke(next)
        } else {
            internalValue = next
            onValueChange?.invoke(next)
        }
    }
}

/**
 * Remembers [ToggleState] across recompositions. The instance survives;
 * [controlledValue], [enabled], and [onValueChange] are refreshed every
 * recomposition. In controlled mode changes only notify.
 */
@Composable
fun rememberToggleState(
    initialValue: ToggleValue = ToggleValue.Off,
    controlledValue: ToggleValue? = null,
    enabled: Boolean = true,
    onValueChange: ((ToggleValue) -> Unit)? = null,
): ToggleState =
    remember {
        ToggleState(initialValue = initialValue, enabled = enabled)
    }.apply {
        this.controlledValue = controlledValue
        this.enabled = enabled
        this.onValueChange = onValueChange
    }
