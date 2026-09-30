package com.biat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import com.biat.ui.core.state.RadioGroupState
import com.biat.ui.core.state.SheetState
import com.biat.ui.primitives.radiogroup.RadioGroup
import com.biat.ui.primitives.radiogroup.RadioGroupContent
import com.biat.ui.primitives.radiogroup.RadioGroupValue
import com.biat.ui.primitives.sheet.Sheet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Caller layouts retain behavior without a library-defined width or axis. */
class HeadlessLayoutTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun radioGroup_callerChoosesHorizontalLayout() {
        rule.setContent {
            RadioGroup(
                state = RadioGroupState(initialSelected = "a"),
                options = listOf(RadioGroupValue("a", "Alpha"), RadioGroupValue("b", "Beta")),
                layout = { content -> Row { content() } },
                item = { entry, _ -> BasicText(entry.label) },
            )
        }
        val alpha = rule.onNodeWithContentDescription("Alpha").fetchSemanticsNode().boundsInRoot
        val beta = rule.onNodeWithContentDescription("Beta").fetchSemanticsNode().boundsInRoot
        assertEquals(alpha.top, beta.top, 0.1f)
        assertTrue(beta.left >= alpha.right)
    }

    @Test
    fun customRadioGroup_retainsArrowSelection() {
        val state = RadioGroupState(initialSelected = "a")
        rule.setContent {
            RadioGroupContent(state) { scope ->
                Row {
                    scope.Item(RadioGroupValue("a", "Alpha")) { BasicText("A") }
                    scope.Item(RadioGroupValue("b", "Beta")) { BasicText("B") }
                }
            }
        }
        val alpha = rule.onNodeWithContentDescription("Alpha")
        alpha.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        alpha.performKeyInput { pressKey(Key.DirectionRight) }
        rule.onNodeWithContentDescription("Beta").assertIsFocused()
        rule.runOnIdle { assertEquals("b", state.selectedValue) }
    }

    @Test
    fun sheet_callerChoosesNarrowTopEndLayoutAndDismissBoundary() {
        val state = SheetState(initialOpen = true)
        rule.setContent {
            Sheet(
                state = state,
                label = "Sheet content",
                layout = { content ->
                    Box(Modifier.width(160.dp).height(80.dp).align(Alignment.TopEnd)) { content() }
                },
                scrim = { Box(Modifier.fillMaxSize().testTag("scrim")) },
            ) {
                BasicText("Body", Modifier.fillMaxSize())
            }
        }
        val content = rule.onNodeWithContentDescription("Sheet content")
        content.assertWidthIsEqualTo(160.dp)
        val bounds = content.fetchSemanticsNode().boundsInRoot
        assertEquals(0f, bounds.top, 0.1f)
        content.performTouchInput { click() }
        rule.runOnIdle { assertTrue(state.isOpen) }
        rule.onNodeWithTag("scrim").performTouchInput { click(Offset(width / 2f, height - 4f)) }
        rule.runOnIdle { assertFalse(state.isOpen) }
    }
}
