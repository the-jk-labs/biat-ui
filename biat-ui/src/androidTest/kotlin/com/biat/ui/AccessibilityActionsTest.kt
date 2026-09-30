package com.biat.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performSemanticsAction
import com.biat.ui.core.state.SliderState
import com.biat.ui.core.state.ToggleState
import com.biat.ui.core.state.ToggleValue
import com.biat.ui.primitives.separator.Separator
import com.biat.ui.primitives.slider.Slider
import com.biat.ui.primitives.toggle.Checkbox
import com.biat.ui.primitives.toggle.ToggleButton
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Verifies actions consumed by accessibility services, beyond label rendering. */
class AccessibilityActionsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun slider_exposesRangeAndSnappedAdjustment() {
        val state = SliderState(initialValue = 4f, valueRange = 0f..10f, step = 2f)
        rule.setContent { Slider(state, label = "Volume", track = {}, thumb = {}) }
        val node = rule.onNodeWithContentDescription("Volume")
        node.assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(4f, 0f..10f, 4)))
        node.performSemanticsAction(SemanticsActions.SetProgress) { assertTrue(it(7f)) }
        rule.runOnIdle { assertEquals(8f, state.value, 0f) }
        node.performSemanticsAction(SemanticsActions.SetProgress) { assertFalse(it(8f)) }
        node.performSemanticsAction(SemanticsActions.SetProgress) { assertTrue(it(30f)) }
        rule.runOnIdle { assertEquals(10f, state.value, 0f) }
    }

    @Test
    fun disabledSlider_hasRangeButNoAdjustmentAction() {
        val state = SliderState(initialValue = 0.5f, enabled = false)
        rule.setContent { Slider(state, label = "Volume", track = {}, thumb = {}) }
        val node = rule.onNodeWithContentDescription("Volume")
        node.assertIsNotEnabled()
        assertFalse(node.fetchSemanticsNode().config.contains(SemanticsActions.SetProgress))
    }

    @Test
    fun controlledSlider_adjustmentNotifiesOwnerWithoutMutation() {
        var requested: Float? = null
        val state = SliderState(controlledValue = 4f, valueRange = 0f..10f, step = 2f, onValueChange = { requested = it })
        rule.setContent { Slider(state, label = "Volume", track = {}, thumb = {}) }
        rule.onNodeWithContentDescription("Volume").performSemanticsAction(SemanticsActions.SetProgress) { assertTrue(it(7f)) }
        rule.runOnIdle {
            assertEquals(8f, requested)
            assertEquals(4f, state.value, 0f)
        }
    }

    @Test
    fun decorativeSeparator_isHiddenFromAccessibility() {
        rule.setContent { Separator { BasicText("Decoration") } }
        rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.HideFromAccessibility)).assertExists()
    }

    @Test
    fun checkbox_exposesNativeIndeterminateStateAndDisabledToggle() {
        val state = ToggleState(initialValue = ToggleValue.Indeterminate, enabled = false)
        rule.setContent { Checkbox(state, label = "Check") { BasicText("Checkbox") } }
        val node = rule.onNodeWithContentDescription("Check")
        node.assertIsNotEnabled()
        node.assert(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.Indeterminate))
        rule.runOnIdle { assertEquals(ToggleValue.Indeterminate, state.value) }
    }

    @Test
    fun disabledToggleButton_exposesDisabledState() {
        rule.setContent { ToggleButton(ToggleState(enabled = false), label = "Bold") { BasicText("B") } }
        rule.onNodeWithContentDescription("Bold").assertIsNotEnabled()
    }
}
