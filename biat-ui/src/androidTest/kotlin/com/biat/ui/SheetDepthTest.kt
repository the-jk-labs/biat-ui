package com.biat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import com.biat.ui.core.state.SheetDetent
import com.biat.ui.core.state.SheetState
import com.biat.ui.primitives.sheet.Sheet
import com.biat.ui.primitives.sheet.sheetDrag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * On-device certification for Sheet depth (v0.3.0): drag stepping between
 * detents, drag dismissal, and opt-in behavior without the modifier.
 */
class SheetDepthTest {
    @get:Rule
    val rule = createComposeRule()

    private fun node(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true)

    @Test
    fun dragDown_stepsFromHalfToPeek() {
        val state = SheetState()
        rule.setContent { DragHarness(state) }
        rule.runOnIdle { state.open(SheetDetent.Half) }
        slowSwipe(0.20f, 0.55f)
        rule.runOnIdle {
            assertEquals(SheetDetent.Peek, state.detent)
            assertTrue(state.isOpen)
        }
    }

    @Test
    fun dragDown_deepDismissesFromHalf() {
        val state = SheetState()
        rule.setContent { DragHarness(state) }
        rule.runOnIdle { state.open(SheetDetent.Half) }
        slowSwipe(0.10f, 0.80f)
        node("drag").assertDoesNotExist()
        rule.runOnIdle { assertFalse(state.isOpen) }
    }

    @Test
    fun dragDown_fromPeekDismisses() {
        val state = SheetState()
        rule.setContent { DragHarness(state) }
        rule.runOnIdle { state.open(SheetDetent.Peek) }
        slowSwipe(0.20f, 0.55f)
        node("drag").assertDoesNotExist()
        rule.runOnIdle { assertFalse(state.isOpen) }
    }

    @Test
    fun dragUp_stepsFromHalfToFull() {
        val state = SheetState()
        rule.setContent { DragHarness(state) }
        rule.runOnIdle { state.open(SheetDetent.Half) }
        slowSwipe(0.80f, 0.45f)
        rule.runOnIdle {
            assertEquals(SheetDetent.Full, state.detent)
            assertTrue(state.isOpen)
        }
    }

    @Test
    fun withoutModifier_swipeDoesNothing() {
        val state = SheetState()
        rule.setContent {
            Sheet(
                layout = { content ->
                    Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter), contentAlignment = Alignment.BottomCenter) { content() }
                },
                state = state,
                dismissOnEscape = false,
                dismissOnBackPress = false,
                dismissOnOutsideClick = false,
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .testTag("drag"),
                ) {
                    BasicText("plain")
                }
            }
        }
        rule.runOnIdle { state.open(SheetDetent.Half) }
        slowSwipe(0.10f, 0.80f)
        rule.runOnIdle {
            assertEquals(SheetDetent.Half, state.detent)
            assertTrue(state.isOpen)
        }
    }

    /**
     * Slow vertical swipe across fractions of the drag node height.
     * Slow enough to stay positional (below the fling line).
     */
    private fun slowSwipe(
        fromFraction: Float,
        toFraction: Float,
    ) {
        val size = node("drag").fetchSemanticsNode().size
        node("drag").performTouchInput {
            swipe(
                start = Offset(size.width * 0.5f, size.height * fromFraction),
                end = Offset(size.width * 0.5f, size.height * toFraction),
                durationMillis = 800,
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun DragHarness(state: SheetState) {
    Sheet(
        layout = { content ->
            Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter), contentAlignment = Alignment.BottomCenter) { content() }
        },
        state = state,
        dismissOnEscape = false,
        dismissOnBackPress = false,
        dismissOnOutsideClick = false,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(400.dp)
                .testTag("drag")
                .sheetDrag(state),
        ) {
            BasicText("drag me")
        }
    }
}
