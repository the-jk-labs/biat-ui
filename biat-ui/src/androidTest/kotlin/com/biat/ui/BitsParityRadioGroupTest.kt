package com.biat.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.biat.ui.core.state.rememberRadioGroupState
import com.biat.ui.primitives.radiogroup.RadioGroup
import com.biat.ui.primitives.radiogroup.RadioGroupValue
import org.junit.Rule
import org.junit.Test

/**
 * On-device certification for the v0.4.0 RadioGroup: options announce their
 * selection state and a click moves the selection.
 */
class BitsParityRadioGroupTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun radioGroup_clickSelectsSecond() {
        rule.setContent {
            RadioGroup(
                state = rememberRadioGroupState(initialSelected = "a"),
                options = listOf(RadioGroupValue("a", "Alpha"), RadioGroupValue("b", "Beta")),
                item = { item, _ -> BasicText(item.label) },
            )
        }
        val beta = rule.onNodeWithContentDescription("Beta", useUnmergedTree = true)
        beta.assertIsNotSelected()
        beta.performClick()
        beta.assertIsSelected()
    }
}
