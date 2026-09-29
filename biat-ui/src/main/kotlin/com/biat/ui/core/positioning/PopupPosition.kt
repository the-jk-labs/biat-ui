package com.biat.ui.core.positioning

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Preferred side of the anchor the popup opens on.
 * Start/End follow the ambient layout direction.
 */
enum class PopupSide {
    Top,
    Bottom,
    Start,
    End,
}

/**
 * Alignment of the popup along the anchor edge perpendicular to [PopupSide].
 * For Top/Bottom sides this is the horizontal axis; for Start/End the vertical.
 * Start/End follow the ambient layout direction on the horizontal axis.
 */
enum class PopupAlign {
    Start,
    Center,
    End,
}

/**
 * Popup placement request. UI-free and unit-testable.
 *
 * - [sideOffsetPx] pushes the popup away from the anchor along [side].
 * - [alignOffsetPx] shifts the popup along the alignment axis (toward
 *   End on the horizontal axis, toward Bottom on the vertical axis).
 * - [avoidCollisions] flips to the opposite side when it overflows less,
 *   then shifts the popup to stay inside the window. When false the
 *   preferred placement is used verbatim, even off-window.
 */
data class PopupPlacement(
    val side: PopupSide = PopupSide.Bottom,
    val align: PopupAlign = PopupAlign.Start,
    val sideOffsetPx: Int = 0,
    val alignOffsetPx: Int = 0,
    val avoidCollisions: Boolean = true,
)

/**
 * Resolves the popup origin in window coordinates. Pure function of its
 * inputs; all collision behavior is pinned by unit tests.
 */
fun resolvePopupOffset(
    anchorBounds: IntRect,
    popupSize: IntSize,
    windowSize: IntSize,
    layoutDirection: LayoutDirection,
    placement: PopupPlacement = PopupPlacement(),
): IntOffset {
    val preferred = offsetFor(
        anchorBounds = anchorBounds,
        popupSize = popupSize,
        side = placement.side,
        align = placement.align,
        sideOffsetPx = placement.sideOffsetPx,
        alignOffsetPx = placement.alignOffsetPx,
        layoutDirection = layoutDirection,
    )
    if (!placement.avoidCollisions) {
        return preferred
    }
    val flipped = offsetFor(
        anchorBounds = anchorBounds,
        popupSize = popupSize,
        side = oppositeSide(placement.side),
        align = placement.align,
        sideOffsetPx = placement.sideOffsetPx,
        alignOffsetPx = placement.alignOffsetPx,
        layoutDirection = layoutDirection,
    )
    val chosen = if (overflowOf(flipped, popupSize, windowSize) <
        overflowOf(preferred, popupSize, windowSize)
    ) {
        flipped
    } else {
        preferred
    }
    return clampToWindow(chosen, popupSize, windowSize)
}

private fun offsetFor(
    anchorBounds: IntRect,
    popupSize: IntSize,
    side: PopupSide,
    align: PopupAlign,
    sideOffsetPx: Int,
    alignOffsetPx: Int,
    layoutDirection: LayoutDirection,
): IntOffset {
    val rtl = layoutDirection == LayoutDirection.Rtl
    // Align offset points toward End on the horizontal axis, so it mirrors
    // in RTL; the vertical axis always points toward Bottom.
    val alignOffsetX = if (rtl) -alignOffsetPx else alignOffsetPx
    return when (side) {
        PopupSide.Bottom -> IntOffset(
            x = alignX(anchorBounds, popupSize.width, align, rtl) + alignOffsetX,
            y = anchorBounds.bottom + sideOffsetPx,
        )
        PopupSide.Top -> IntOffset(
            x = alignX(anchorBounds, popupSize.width, align, rtl) + alignOffsetX,
            y = anchorBounds.top - popupSize.height - sideOffsetPx,
        )
        PopupSide.End -> IntOffset(
            x = if (!rtl) {
                anchorBounds.right + sideOffsetPx
            } else {
                anchorBounds.left - popupSize.width - sideOffsetPx
            },
            y = alignY(anchorBounds, popupSize.height, align) + alignOffsetPx,
        )
        PopupSide.Start -> IntOffset(
            x = if (!rtl) {
                anchorBounds.left - popupSize.width - sideOffsetPx
            } else {
                anchorBounds.right + sideOffsetPx
            },
            y = alignY(anchorBounds, popupSize.height, align) + alignOffsetPx,
        )
    }
}

private fun alignX(
    anchorBounds: IntRect,
    popupWidth: Int,
    align: PopupAlign,
    rtl: Boolean,
): Int = when (align) {
    PopupAlign.Start -> if (!rtl) anchorBounds.left else anchorBounds.right - popupWidth
    PopupAlign.Center -> anchorBounds.left + (anchorBounds.width - popupWidth) / 2
    PopupAlign.End -> if (!rtl) anchorBounds.right - popupWidth else anchorBounds.left
}

private fun alignY(
    anchorBounds: IntRect,
    popupHeight: Int,
    align: PopupAlign,
): Int = when (align) {
    PopupAlign.Start -> anchorBounds.top
    PopupAlign.Center -> anchorBounds.top + (anchorBounds.height - popupHeight) / 2
    PopupAlign.End -> anchorBounds.bottom - popupHeight
}

private fun oppositeSide(side: PopupSide): PopupSide = when (side) {
    PopupSide.Top -> PopupSide.Bottom
    PopupSide.Bottom -> PopupSide.Top
    PopupSide.Start -> PopupSide.End
    PopupSide.End -> PopupSide.Start
}

/** Pixels of [offset]-placed popup outside [windowSize]. Zero means fully visible. */
private fun overflowOf(
    offset: IntOffset,
    popupSize: IntSize,
    windowSize: IntSize,
): Int = max(0, -offset.x) +
    max(0, offset.x + popupSize.width - windowSize.width) +
    max(0, -offset.y) +
    max(0, offset.y + popupSize.height - windowSize.height)

/** Shifts [offset] so the popup stays inside the window. Oversized popups pin to 0. */
private fun clampToWindow(
    offset: IntOffset,
    popupSize: IntSize,
    windowSize: IntSize,
): IntOffset = IntOffset(
    x = offset.x.coerceIn(0, max(0, windowSize.width - popupSize.width)),
    y = offset.y.coerceIn(0, max(0, windowSize.height - popupSize.height)),
)

/**
 * [PopupPositionProvider] driven by [PopupPlacement]. Passed to
 * `androidx.compose.ui.window.Popup` so anchored primitives share one
 * placement engine. The caller still owns every pixel inside the popup.
 */
class BiatPopupPositionProvider(
    val placement: PopupPlacement,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = resolvePopupOffset(
        anchorBounds = anchorBounds,
        popupSize = popupContentSize,
        windowSize = windowSize,
        layoutDirection = layoutDirection,
        placement = placement,
    )
}

/**
 * Remembers a [BiatPopupPositionProvider] from Dp offsets. Density is read
 * here so the pure resolver and [PopupPlacement] stay in pixels and testable.
 */
@Composable
fun rememberBiatPopupPosition(
    side: PopupSide = PopupSide.Bottom,
    align: PopupAlign = PopupAlign.Start,
    sideOffset: Dp = 0.dp,
    alignOffset: Dp = 0.dp,
    avoidCollisions: Boolean = true,
): PopupPositionProvider {
    val density = LocalDensity.current
    val placement = PopupPlacement(
        side = side,
        align = align,
        sideOffsetPx = with(density) { sideOffset.toPx().roundToInt() },
        alignOffsetPx = with(density) { alignOffset.toPx().roundToInt() },
        avoidCollisions = avoidCollisions,
    )
    return remember(placement) { BiatPopupPositionProvider(placement) }
}
