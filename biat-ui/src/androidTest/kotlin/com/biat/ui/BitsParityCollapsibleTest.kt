package com.biat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.biat.ui.core.state.AccordionType
import com.biat.ui.core.state.rememberAccordionState
import com.biat.ui.core.state.rememberCollapsibleState
import com.biat.ui.primitives.accordion.Accordion
import com.biat.ui.primitives.accordion.AccordionValue
import com.biat.ui.primitives.collapsible.Collapsible
import org.junit.Rule
import org.junit.Test

/**
 * On-device certification for the v0.4.0 Collapsible and Accordion: clicking a
 * trigger opens the panel, clicking it again closes it, and a Multiple
 * accordion keeps two panels open at once.
 */
class BitsParityCollapsibleTest {
    @get:Rule
    val rule = createComposeRule()

    private fun node(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true)

    @Test
    fun collapsible_clickTogglesPanel() {
        rule.setContent {
            Collapsible(
                layout = { content -> Column { content() } },
                state = rememberCollapsibleState(),
                trigger = { Box(Modifier.testTag("ctrigger")) { BasicText("Trigger") } },
            ) {
                BasicText("Panel")
            }
        }
        rule.onNodeWithText("Panel", useUnmergedTree = true).assertDoesNotExist()
        node("ctrigger").performClick()
        rule.onNodeWithText("Panel", useUnmergedTree = true).assertIsDisplayed()
        node("ctrigger").performClick()
        rule.onNodeWithText("Panel", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun accordion_multipleKeepsTwoPanelsOpen() {
        val items = listOf(AccordionValue("a", "A"), AccordionValue("b", "B"))
        rule.setContent {
            Accordion(
                layout = { content -> Column { content() } },
                state = rememberAccordionState(type = AccordionType.Multiple),
                items = items,
                trigger = { item, _ ->
                    Box(Modifier.testTag("atrigger-${item.value}")) { BasicText(item.label) }
                },
            ) { item ->
                BasicText("Panel-${item.value}")
            }
        }
        node("atrigger-a").performClick()
        node("atrigger-b").performClick()
        rule.onNodeWithText("Panel-a", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("Panel-b", useUnmergedTree = true).assertIsDisplayed()
    }
}
