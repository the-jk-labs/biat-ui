package com.biat.ui

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.biat.ui.core.positioning.PopupAlign
import com.biat.ui.core.positioning.PopupPlacement
import com.biat.ui.core.positioning.PopupSide
import com.biat.ui.core.positioning.resolvePopupOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class PositioningTest {

    private val window = IntSize(720, 1600)
    private val popup = IntSize(120, 80)

    private fun resolve(
        anchor: IntRect,
        popupSize: IntSize = popup,
        placement: PopupPlacement = PopupPlacement(),
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    ): IntOffset = resolvePopupOffset(
        anchorBounds = anchor,
        popupSize = popupSize,
        windowSize = window,
        layoutDirection = layoutDirection,
        placement = placement,
    )

    @Test
    fun default_opensBelowAnchorStart() {
        assertEquals(
            IntOffset(100, 260),
            resolve(IntRect(100, 200, 300, 260)),
        )
    }

    @Test
    fun sideOffset_pushesAwayFromAnchor() {
        assertEquals(
            IntOffset(100, 268),
            resolve(
                anchor = IntRect(100, 200, 300, 260),
                placement = PopupPlacement(sideOffsetPx = 8),
            ),
        )
        assertEquals(
            IntOffset(100, 112),
            resolve(
                anchor = IntRect(100, 200, 300, 260),
                placement = PopupPlacement(side = PopupSide.Top, sideOffsetPx = 8),
            ),
        )
    }

    @Test
    fun alignCenter_centersOnAnchor() {
        assertEquals(
            IntOffset(140, 260),
            resolve(
                anchor = IntRect(100, 200, 300, 260),
                placement = PopupPlacement(align = PopupAlign.Center),
            ),
        )
    }

    @Test
    fun alignEnd_alignsToAnchorEnd() {
        assertEquals(
            IntOffset(180, 260),
            resolve(
                anchor = IntRect(100, 200, 300, 260),
                placement = PopupPlacement(align = PopupAlign.End),
            ),
        )
    }

    @Test
    fun alignOffset_shiftsAlongAlignAxis() {
        assertEquals(
            IntOffset(110, 260),
            resolve(
                anchor = IntRect(100, 200, 300, 260),
                placement = PopupPlacement(alignOffsetPx = 10),
            ),
        )
    }

    @Test
    fun bottomOverflow_flipsToTop() {
        assertEquals(
            IntOffset(100, 1300),
            resolve(
                anchor = IntRect(100, 1500, 300, 1560),
                popupSize = IntSize(120, 200),
            ),
        )
    }

    @Test
    fun topOverflow_flipsToBottom() {
        assertEquals(
            IntOffset(100, 100),
            resolve(
                anchor = IntRect(100, 40, 300, 100),
                popupSize = IntSize(120, 200),
                placement = PopupPlacement(side = PopupSide.Top),
            ),
        )
    }

    @Test
    fun avoidCollisionsFalse_keepsOverflowingPlacement() {
        assertEquals(
            IntOffset(100, 1560),
            resolve(
                anchor = IntRect(100, 1500, 300, 1560),
                popupSize = IntSize(120, 200),
                placement = PopupPlacement(avoidCollisions = false),
            ),
        )
    }

    @Test
    fun horizontalOverflow_shiftsIntoWindow() {
        assertEquals(
            IntOffset(600, 260),
            resolve(IntRect(650, 200, 700, 260)),
        )
    }

    @Test
    fun endSide_opensAfterAnchor() {
        assertEquals(
            IntOffset(300, 200),
            resolve(
                anchor = IntRect(100, 200, 300, 260),
                placement = PopupPlacement(side = PopupSide.End),
            ),
        )
    }

    @Test
    fun startOverflow_flipsToEnd() {
        assertEquals(
            IntOffset(300, 200),
            resolve(
                anchor = IntRect(100, 200, 300, 260),
                placement = PopupPlacement(side = PopupSide.Start),
            ),
        )
    }

    @Test
    fun rtl_startAlign_usesRightEdge() {
        assertEquals(
            IntOffset(180, 260),
            resolve(
                anchor = IntRect(100, 200, 300, 260),
                layoutDirection = LayoutDirection.Rtl,
            ),
        )
    }

    @Test
    fun rtl_endSide_opensBeforeAnchor() {
        assertEquals(
            IntOffset(300, 200),
            resolve(
                anchor = IntRect(100, 200, 300, 260),
                placement = PopupPlacement(side = PopupSide.End),
                layoutDirection = LayoutDirection.Rtl,
            ),
        )
    }

    @Test
    fun oversizedPopup_pinsToOrigin() {
        assertEquals(
            IntOffset(0, 0),
            resolve(
                anchor = IntRect(300, 700, 420, 760),
                popupSize = IntSize(900, 2000),
            ),
        )
    }
}
