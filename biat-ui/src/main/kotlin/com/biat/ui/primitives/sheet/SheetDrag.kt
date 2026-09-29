package com.biat.ui.primitives.sheet

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import com.biat.ui.core.state.SheetDetent
import com.biat.ui.core.state.SheetSettle
import com.biat.ui.core.state.SheetState
import com.biat.ui.core.state.resolveSheetSettle
import kotlin.math.roundToInt

/**
 * Headless sheet drag behavior. Attach to the caller's drag handle (or any
 * sheet region) to get drag-to-dismiss, detent stepping, and fling support
 * with zero visuals: the node follows the finger while held and the sheet
 * settles on release via [resolveSheetSettle].
 *
 * Fractions ([dismissFraction]/[stepFraction]) are measured against
 * [sheetHeightPx] when given, otherwise against the attached node's height.
 * Pass the full sheet height when attaching to a small handle (a 48dp
 * handle would otherwise dismiss on a ~24dp drag); attaching to the sheet
 * root needs no override.
 *
 * - Down past [dismissFraction] of the sheet height dismisses; down past
 *   [stepFraction] steps one enabled stop down (dismissing from the lowest);
 *   up past [stepFraction] steps one stop up; flings move one stop in the
 *   fling direction. Anything less snaps back with no state change.
 * - Only [detents] participate; pass a subset to lock the sheet to stops.
 * - When [enabled] is false gestures are ignored entirely.
 * - The offset is transient: it resets on release and nothing persists
 *   across open/close (the modifier leaves composition with the sheet).
 */
fun Modifier.sheetDrag(
    state: SheetState,
    enabled: Boolean = true,
    detents: Set<SheetDetent> = SheetDetent.entries.toSet(),
    dismissFraction: Float = 0.5f,
    stepFraction: Float = 0.25f,
    flingVelocityPxPerSec: Float = 1500f,
    sheetHeightPx: Float? = null,
): Modifier =
    composed {
        var offsetPx by remember { mutableFloatStateOf(0f) }
        var heightPx by remember { mutableIntStateOf(0) }
        this
            .onGloballyPositioned { heightPx = it.size.height }
            .offset { IntOffset(0, offsetPx.roundToInt()) }
            .draggable(
                state =
                    rememberDraggableState { delta ->
                        if (enabled) offsetPx += delta
                    },
                orientation = Orientation.Vertical,
                enabled = enabled,
                onDragStopped = { velocity ->
                    if (!enabled) return@draggable
                    val drag = offsetPx
                    offsetPx = 0f
                    when (
                        val target =
                            resolveSheetSettle(
                                from = state.detent,
                                enabled = detents,
                                dragPx = drag,
                                sheetHeightPx = sheetHeightPx ?: heightPx.toFloat(),
                                velocityPxPerSec = velocity,
                                dismissFraction = dismissFraction,
                                stepFraction = stepFraction,
                                flingVelocityPxPerSec = flingVelocityPxPerSec,
                            )
                    ) {
                        SheetSettle.Dismiss -> state.close()
                        is SheetSettle.Snap -> state.snapTo(target.detent)
                    }
                },
            )
    }
