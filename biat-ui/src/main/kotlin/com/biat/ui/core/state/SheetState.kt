package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Overlay sheet state. [detent] is the current stop (peek/half/full);
 * [isExpanded] is Full vs anything else.
 *
 * Openness contract (controlled vs uncontrolled):
 * - Uncontrolled (default): pass [initialOpen]; the state owns the value and
 *   [open]/[close]/[toggle]/[setOpen] mutate it, notifying [onOpenChange].
 * - Controlled: pass non-null [controlledOpen] plus [onOpenChange]; the caller
 *   owns the value and [open]/[close]/[toggle]/[setOpen] only notify via
 *   [onOpenChange] without mutating. The caller must reflect the new value
 *   back into [controlledOpen] ([rememberSheetState] does this every
 *   recomposition). Do not switch modes during the state's lifetime.
 * - [detent] itself ([snapTo]/[expand]/[collapse]) stays uncontrolled; every
 *   change is mirrored to [onDetentChange]. The caller renders content per
 *   detent (e.g. different heights) and attaches drag via the sheet-drag
 *   behavior modifier.
 */
@Stable
class SheetState(
    initialOpen: Boolean = false,
    initialDetent: SheetDetent = SheetDetent.Half,
    controlledOpen: Boolean? = null,
    var onOpenChange: ((Boolean) -> Unit)? = null,
    var onDetentChange: ((SheetDetent) -> Unit)? = null,
) {
    private var internalOpen by mutableStateOf(initialOpen)

    /**
     * Caller-owned value in controlled mode. Managed by [rememberSheetState];
     * assign only to reflect the caller's value.
     */
    var controlledOpen: Boolean? by mutableStateOf(controlledOpen)

    val isOpen: Boolean get() = controlledOpen ?: internalOpen

    /** Current stop. Renders drive content (e.g. height) from this. */
    var detent by mutableStateOf(initialDetent)
        private set

    /** True when [detent] is [SheetDetent.Full]. */
    val isExpanded: Boolean get() = detent == SheetDetent.Full

    fun open(expanded: Boolean = false) = open(
        if (expanded) SheetDetent.Full else SheetDetent.Half,
    )

    fun open(detent: SheetDetent) {
        snapTo(detent)
        setOpen(true)
    }
    fun close() = setOpen(false)
    fun toggle() = setOpen(!isOpen)

    @JvmName("setOpenState")
    fun setOpen(open: Boolean) {
        if (isOpen == open) return
        if (controlledOpen != null) {
            onOpenChange?.invoke(open)
        } else {
            internalOpen = open
            onOpenChange?.invoke(open)
        }
    }

    fun expand() = snapTo(SheetDetent.Full)
    fun collapse() = snapTo(SheetDetent.Half)

    /**
     * Moves to [detent], notifying [onDetentChange]. No-op when unchanged.
     */
    fun snapTo(detent: SheetDetent) {
        if (this.detent == detent) return
        this.detent = detent
        onDetentChange?.invoke(detent)
    }
}

/** Sheet stops, ordered low to high. */
enum class SheetDetent { Peek, Half, Full }

/** Drag-release outcome: dismiss the sheet or snap to a stop. */
sealed interface SheetSettle {
    data object Dismiss : SheetSettle
    data class Snap(val detent: SheetDetent) : SheetSettle
}

/**
 * Pure release resolver for sheet drags. [dragPx] and [velocityPxPerSec] are
 * positive downward. Only [enabled] stops participate; neighbors are the
 * adjacent enabled stops around [from].
 *
 * - Fast fling (|[velocity]| >= [flingVelocityPxPerSec]): one stop in the
 *   fling direction; flinging down from the lowest enabled stop dismisses.
 * - Otherwise positional against [sheetHeightPx]: down past
 *   [dismissFraction] dismisses; down past [stepFraction] steps one stop
 *   down (dismissing from the lowest); up past [stepFraction] steps one
 *   stop up; anything less snaps back to [from].
 */
fun resolveSheetSettle(
    from: SheetDetent,
    enabled: Set<SheetDetent> = SheetDetent.entries.toSet(),
    dragPx: Float,
    sheetHeightPx: Float,
    velocityPxPerSec: Float,
    dismissFraction: Float = 0.5f,
    stepFraction: Float = 0.25f,
    flingVelocityPxPerSec: Float = 1500f,
): SheetSettle {
    val ordered = SheetDetent.entries.filter { it in enabled }
    if (ordered.isEmpty() || sheetHeightPx <= 0f) return SheetSettle.Snap(from)
    val index = ordered.indexOf(from).takeIf { it >= 0 } ?: ordered.indexOf(
        SheetDetent.Half,
    ).takeIf { it >= 0 } ?: 0
    fun lower(): SheetSettle = if (index > 0) {
        SheetSettle.Snap(ordered[index - 1])
    } else {
        SheetSettle.Dismiss
    }
    fun higher(): SheetSettle = SheetSettle.Snap(
        ordered[(index + 1).coerceAtMost(ordered.lastIndex)],
    )
    if (velocityPxPerSec <= -flingVelocityPxPerSec) return higher()
    if (velocityPxPerSec >= flingVelocityPxPerSec) return lower()
    val progress = dragPx / sheetHeightPx
    return when {
        progress >= dismissFraction -> SheetSettle.Dismiss
        progress >= stepFraction -> lower()
        progress <= -stepFraction -> higher()
        else -> SheetSettle.Snap(from)
    }
}

@Composable
fun rememberSheetState(
    initialOpen: Boolean = false,
    initialDetent: SheetDetent = SheetDetent.Half,
    controlledOpen: Boolean? = null,
    onOpenChange: ((Boolean) -> Unit)? = null,
    onDetentChange: ((SheetDetent) -> Unit)? = null,
): SheetState = remember {
    SheetState(initialOpen = initialOpen, initialDetent = initialDetent)
}.apply {
    this.controlledOpen = controlledOpen
    this.onOpenChange = onOpenChange
    this.onDetentChange = onDetentChange
}
