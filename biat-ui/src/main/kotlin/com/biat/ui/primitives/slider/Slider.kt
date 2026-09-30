package com.biat.ui.primitives.slider

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.biat.ui.core.accessibility.sliderSemantics
import com.biat.ui.core.state.SliderState
import com.biat.ui.core.state.rememberSliderState

/**
 * Headless Slider. Behavior only, zero styling.
 *
 * - [track] renders the bar with the current 0..1 [fraction]; [thumb] renders
 *   the handle. Both are caller visuals entirely.
 * - Horizontal drag maps pointer distance to [SliderState.setFraction] using
 *   the measured track width; keyboard Left/Down decrements, Right/Up
 *   increments by [SliderState.step] (or 5 percent continuous), Home/End jump
 *   to min/max, PageUp/PageDown move 10 percent.
 * - [label] names the slider for screen readers; [valueText] announces the
 *   value (defaults to the raw float). Range information and a snapped
 *   SetProgress accessibility action are exposed while enabled.
 */
@Composable
fun Slider(
    state: SliderState = rememberSliderState(),
    enabled: Boolean = true,
    label: String? = null,
    valueText: String? = null,
    track: @Composable (fraction: Float) -> Unit,
    thumb: @Composable () -> Unit,
) {
    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    Box(
        modifier =
            Modifier
                .sliderSemantics(
                    value = state.value,
                    valueText = valueText,
                    enabled = enabled && state.enabled,
                    label = label,
                    valueRange = state.valueRange,
                    step = state.step,
                    onValueChange = { state.setValue(it) },
                ).onSizeChanged { trackWidthPx = it.width.toFloat() }
                .focusable(enabled = enabled && state.enabled)
                .pointerInput(enabled, state) {
                    if (!enabled || !state.enabled) return@pointerInput
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        if (trackWidthPx > 0f) {
                            state.setFraction(state.fraction + dragAmount / trackWidthPx)
                        }
                    }
                }.onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    if (!enabled || !state.enabled) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.DirectionLeft, Key.DirectionDown -> {
                            state.decrease()
                            true
                        }

                        Key.DirectionRight, Key.DirectionUp -> {
                            state.increase()
                            true
                        }

                        Key.MoveHome -> {
                            state.toMin()
                            true
                        }

                        Key.MoveEnd -> {
                            state.toMax()
                            true
                        }

                        Key.PageUp -> {
                            state.setFraction(state.fraction + 0.1f)
                            true
                        }

                        Key.PageDown -> {
                            state.setFraction(state.fraction - 0.1f)
                            true
                        }

                        else -> {
                            false
                        }
                    }
                },
    ) {
        track(state.fraction)
        thumb()
    }
}
