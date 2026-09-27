package com.biat.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.biat.ui.core.state.ToggleValue
import com.biat.ui.core.state.rememberToggleState
import com.biat.ui.primitives.toggle.Checkbox
import com.biat.ui.primitives.toggle.Switch
import org.junit.Rule
import org.junit.Test

/**
 * On-device certification for the v0.4.0 headless toggles: the checkbox
 * announces selection and flips on click, and a disabled switch refuses
 * clicks while still reporting itself disabled.
 */
class BitsParityToggleTest {

    @get:Rule
    val rule = createComposeRule()

    private fun node(label: String) =
        rule.onNodeWithContentDescription(label, useUnmergedTree = true)

    @Test
    fun checkbox_clickChecksAndAnnouncesSelected() {
        rule.setContent {
            Checkbox(state = rememberToggleState(), label = "Accept") {
                BasicText("Box")
            }
        }
        node("Accept").assertIsNotSelected()
        node("Accept").performClick()
        node("Accept").assertIsSelected()
    }

    @Test
    fun switch_disabledRefusesClick() {
        rule.setContent {
            Switch(
                state = rememberToggleState(initialValue = ToggleValue.Off),
                enabled = false,
                label = "Alerts",
            ) {
                BasicText(if (it) "On" else "Off")
            }
        }
        node("Alerts").assertIsNotEnabled()
        rule.onNodeWithText("Off", useUnmergedTree = true).assertIsDisplayed()
        node("Alerts").performClick()
        rule.onNodeWithText("Off", useUnmergedTree = true).assertIsDisplayed()
    }
}
