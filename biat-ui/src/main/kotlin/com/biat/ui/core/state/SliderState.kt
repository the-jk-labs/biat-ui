package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt

/**
 * Logic-only state for a headless Slider. No UI, no styling.
 *
 * Value contract (controlled vs uncontrolled):
 * - Uncontrolled (default): pass [initialValue]; the state owns the value and
 *   [setValue]/[increase]/[decrease] mutate it, notifying [onValueChange].
 * - Controlled: pass non-null [controlledValue] plus [onValueChange]; the caller
 *   owns the value and mutators only notify without mutating. The caller must
 *   reflect the new value back into [controlledValue]
 *   ([rememberSliderState] does this every recomposition).
 * - When [enabled] is false mutators are ignored entirely (no notify).
 *
 * [valueRange] is inclusive min..max. [step] is the keyboard/drag quantum;
 * step <= 0 means continuous. Values are clamped then snapped to the step grid.
 */
@Stable
class SliderState(
    initialValue: Float = 0f,
    controlledValue: Float? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    step: Float = 0f,
    var enabled: Boolean = true,
    var onValueChange: ((Float) -> Unit)? = null,
) {
    private var internalValue by mutableFloatStateOf(
        coerceAndSnap(initialValue, valueRange, step),
    )

    /**
     * Caller-owned value in controlled mode. Managed by [rememberSliderState];
     * assign only to reflect the caller's value.
     */
    var controlledValue: Float? by mutableStateOf(controlledValue)

    /** Range config. Managed by [rememberSliderState]; assign only to reflect the caller's value. */
    var valueRange: ClosedFloatingPointRange<Float> by mutableStateOf(valueRange)

    /** Step config. Managed by [rememberSliderState]; assign only to reflect the caller's value. */
    var step: Float by mutableStateOf(step)

    val value: Float get() = controlledValue ?: internalValue

    /** Fraction of [value] within [valueRange], 0..1. */
    val fraction: Float get() = valueToFraction(value, valueRange)

    fun setValue(next: Float) {
        if (!enabled) return
        val snapped = coerceAndSnap(next, valueRange, step)
        if (snapped == value) return
        if (controlledValue != null) {
            onValueChange?.invoke(snapped)
        } else {
            internalValue = snapped
            onValueChange?.invoke(snapped)
        }
    }

    fun setFraction(fraction: Float) {
        setValue(fractionToValue(fraction, valueRange))
    }

    fun increase(steps: Int = 1) {
        val delta = if (step > 0f) step * steps else rangeSize(valueRange) * 0.05f * steps
        setValue(value + delta)
    }

    fun decrease(steps: Int = 1) {
        val delta = if (step > 0f) step * steps else rangeSize(valueRange) * 0.05f * steps
        setValue(value - delta)
    }

    fun toMin() = setValue(valueRange.start)

    fun toMax() = setValue(valueRange.endInclusive)

    /**
     * Reflects caller config without notifying: updates [valueRange]/[step]
     * and silently re-snaps the uncontrolled value so it never renders
     * outside the new range. Used by [rememberSliderState].
     */
    internal fun updateConfig(
        valueRange: ClosedFloatingPointRange<Float>,
        step: Float,
    ) {
        this.valueRange = valueRange
        this.step = step
        if (controlledValue == null) {
            internalValue = coerceAndSnap(internalValue, valueRange, step)
        }
    }
}

@Composable
fun rememberSliderState(
    initialValue: Float = 0f,
    controlledValue: Float? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    step: Float = 0f,
    enabled: Boolean = true,
    onValueChange: ((Float) -> Unit)? = null,
): SliderState =
    remember {
        SliderState(
            initialValue = initialValue,
            enabled = enabled,
        )
    }.apply {
        updateConfig(valueRange, step)
        this.controlledValue = controlledValue
        this.enabled = enabled
        this.onValueChange = onValueChange
    }

/** Clamp [value] into [range] then snap to the [step] grid. Pure. */
fun coerceAndSnap(
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
): Float {
    val clamped = value.coerceIn(range.start, range.endInclusive)
    if (step <= 0f) return clamped
    val steps = ((clamped - range.start) / step).roundToInt()
    return (range.start + steps * step).coerceIn(range.start, range.endInclusive)
}

/** Map [value] to 0..1 within [range]. Pure. */
fun valueToFraction(
    value: Float,
    range: ClosedFloatingPointRange<Float>,
): Float {
    val size = range.endInclusive - range.start
    if (size <= 0f) return 0f
    return ((value - range.start) / size).coerceIn(0f, 1f)
}

/** Map 0..1 [fraction] to a value within [range]. Pure. */
fun fractionToValue(
    fraction: Float,
    range: ClosedFloatingPointRange<Float>,
): Float {
    val size = range.endInclusive - range.start
    return range.start + fraction.coerceIn(0f, 1f) * size
}

private fun rangeSize(range: ClosedFloatingPointRange<Float>): Float = (range.endInclusive - range.start).coerceAtLeast(0f)
