package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Selection model for a headless toggle group.
 *
 * - [Single]: at most one value pressed. Pressing the pressed value clears it
 *   when [allowDeselect] is true, otherwise it stays.
 * - [Multiple]: any subset pressed. Pressing toggles membership.
 */
enum class ToggleGroupType { Single, Multiple }

/**
 * Logic-only state for ToggleGroup. No UI, no styling.
 *
 * Pressed values are caller keys ([Any]). Every real change is mirrored to
 * [onPressedChange]. When [enabled] is false [toggle]/[press]/[release] are
 * ignored entirely.
 */
@Stable
class ToggleGroupState(
    initialPressed: List<Any?> = emptyList(),
    val type: ToggleGroupType = ToggleGroupType.Single,
    val allowDeselect: Boolean = true,
    var enabled: Boolean = true,
    var onPressedChange: ((List<Any?>) -> Unit)? = null,
) {
    var pressedValues: List<Any?> by mutableStateOf(
        if (type == ToggleGroupType.Single) initialPressed.take(1) else initialPressed.toList(),
    )
        private set

    fun isPressed(value: Any?): Boolean = pressedValues.contains(value)

    fun toggle(value: Any?) {
        if (!enabled) return
        if (type == ToggleGroupType.Single) {
            if (isPressed(value)) {
                if (allowDeselect) {
                    pressedValues = emptyList()
                    onPressedChange?.invoke(pressedValues)
                }
            } else {
                pressedValues = listOf(value)
                onPressedChange?.invoke(pressedValues)
            }
        } else {
            pressedValues =
                if (isPressed(value)) {
                    pressedValues.filterNot { it == value }
                } else {
                    pressedValues + value
                }
            onPressedChange?.invoke(pressedValues)
        }
    }

    fun press(value: Any?) {
        if (!enabled || isPressed(value)) return
        pressedValues = if (type == ToggleGroupType.Single) listOf(value) else pressedValues + value
        onPressedChange?.invoke(pressedValues)
    }

    fun release(value: Any?) {
        if (!enabled || !isPressed(value)) return
        if (type == ToggleGroupType.Single && !allowDeselect && pressedValues.size <= 1) return
        pressedValues = pressedValues.filterNot { it == value }
        onPressedChange?.invoke(pressedValues)
    }
}

/**
 * Remembers [ToggleGroupState] across recompositions. [type],
 * [allowDeselect], [enabled], and [onPressedChange] are refreshed every
 * recomposition so handlers never go stale.
 */
@Composable
fun rememberToggleGroupState(
    initialPressed: List<Any?> = emptyList(),
    type: ToggleGroupType = ToggleGroupType.Single,
    allowDeselect: Boolean = true,
    enabled: Boolean = true,
    onPressedChange: ((List<Any?>) -> Unit)? = null,
): ToggleGroupState =
    remember {
        ToggleGroupState(
            initialPressed = initialPressed,
            type = type,
            allowDeselect = allowDeselect,
            enabled = enabled,
        )
    }.apply {
        this.enabled = enabled
        this.onPressedChange = onPressedChange
    }
