package com.biat.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import com.biat.ui.core.state.MenuState
import com.biat.ui.core.state.RadioGroupState
import com.biat.ui.core.state.TabsState
import com.biat.ui.core.state.ToggleGroupState
import com.biat.ui.core.state.ToolbarState
import com.biat.ui.primitives.menu.Menu
import com.biat.ui.primitives.menu.MenuItem
import com.biat.ui.primitives.radiogroup.RadioGroup
import com.biat.ui.primitives.radiogroup.RadioGroupValue
import com.biat.ui.primitives.tabs.TabValue
import com.biat.ui.primitives.tabs.Tabs
import com.biat.ui.primitives.togglegroup.ToggleGroup
import com.biat.ui.primitives.togglegroup.ToggleGroupValue
import com.biat.ui.primitives.toolbar.Toolbar
import com.biat.ui.primitives.toolbar.ToolbarValue
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** End-to-end keyboard tests using the primitives' own focus targets. */
class GroupKeyboardTest {
    @get:Rule
    val rule = createComposeRule()

    private fun item(label: String) = rule.onNodeWithContentDescription(label)

    @Test
    fun menu_skipsDisabledActivatesAndReturnsFocus() {
        val state = MenuState(itemCount = 3, disabledIndices = setOf(1))
        var chosen = "none"
        rule.setContent {
            Menu(state = state, label = "Actions", trigger = { BasicText("Open") }) {
                listOf("Edit", "Delete", "Copy").forEachIndexed { index, label ->
                    MenuItem(state, enabled = index != 1, label = label, onSelect = { chosen = label }) {
                        BasicText(label)
                    }
                }
            }
        }
        item("Actions").performClick()
        item("Edit").assertIsFocused()
        item("Edit").performKeyInput { pressKey(Key.DirectionDown) }
        item("Copy").assertIsFocused()
        rule.runOnIdle { assertEquals(2, state.highlightedIndex) }
        item("Copy").performKeyInput { pressKey(Key.Enter) }
        rule.waitUntil(5_000) {
            runCatching { item("Actions").fetchSemanticsNode().config[SemanticsProperties.Focused] }.getOrDefault(false)
        }
        item("Actions").assertIsFocused()
        rule.runOnIdle { assertEquals("Copy", chosen) }
    }

    @Test
    fun radio_arrowsSelectSkipDisabledAndWrap() {
        val state = RadioGroupState(initialSelected = "a")
        rule.setContent {
            RadioGroup(
                state,
                options =
                    listOf(
                        RadioGroupValue("a", "Alpha"),
                        RadioGroupValue("b", "Beta"),
                        RadioGroupValue("c", "Gamma"),
                    ),
                disabledValues = setOf("b"),
                item = { entry, _ -> BasicText(entry.label) },
            )
        }
        item("Alpha").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        item("Alpha").performKeyInput { pressKey(Key.DirectionDown) }
        item("Gamma").assertIsFocused()
        rule.runOnIdle { assertEquals("c", state.selectedValue) }
        item("Gamma").performKeyInput { pressKey(Key.DirectionDown) }
        item("Alpha").assertIsFocused()
        rule.runOnIdle { assertEquals("a", state.selectedValue) }
    }

    @Test
    fun toggle_arrowsMoveFocusEnterActivatesTabExits() {
        val state = ToggleGroupState()
        rule.setContent {
            Column {
                ToggleGroup(
                    state,
                    items = listOf(ToggleGroupValue("a", "Bold"), ToggleGroupValue("b", "Italic")),
                    item = { entry, _ -> BasicText(entry.label) },
                )
                BasicText("After", Modifier.focusable().testTag("after"))
            }
        }
        item("Bold").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        item("Bold").performKeyInput { pressKey(Key.DirectionRight) }
        item("Italic").assertIsFocused()
        rule.runOnIdle { assertEquals(emptyList<Any?>(), state.pressedValues) }
        item("Italic").performKeyInput { pressKey(Key.Enter) }
        rule.runOnIdle { assertEquals(listOf("b"), state.pressedValues) }
        item("Italic").performKeyInput { pressKey(Key.Tab) }
        rule.onNodeWithTag("after").assertIsFocused()
    }

    @Test
    fun tabs_tabSkipsOtherTabsAndArrowStartsAtClickedTab() {
        val state = TabsState(initialSelected = "a")
        rule.setContent {
            Column {
                Tabs(
                    state,
                    tabs = listOf(TabValue("a", "Alpha"), TabValue("b", "Beta"), TabValue("c", "Gamma")),
                    tab = { entry, _, _ -> BasicText(entry.label) },
                    panel = {},
                )
                BasicText("After", Modifier.focusable().testTag("after"))
            }
        }
        item("Alpha").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        item("Alpha").performKeyInput { pressKey(Key.Tab) }
        rule.onNodeWithTag("after").assertIsFocused()
        item("Beta").performClick()
        // Selection moves the entry tab stop without stealing focus from the next control.
        item("Beta").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        item("Beta").performKeyInput { pressKey(Key.DirectionRight) }
        item("Gamma").assertIsFocused()
        rule.runOnIdle { assertEquals("c", state.selectedValue) }
    }

    @Test
    fun toolbar_disabledSkipAndOneTabStop() {
        val state = ToolbarState(itemCount = 3, disabledIndices = setOf(1))
        rule.setContent {
            Column {
                Toolbar(
                    state,
                    items = listOf(ToolbarValue("a", "Cut"), ToolbarValue("b", "Paste"), ToolbarValue("c", "Copy")),
                    item = { entry, _ -> BasicText(entry.label) },
                )
                BasicText("After", Modifier.focusable().testTag("after"))
            }
        }
        item("Cut").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        item("Cut").performKeyInput { pressKey(Key.DirectionRight) }
        item("Copy").assertIsFocused()
        rule.runOnIdle { assertEquals(2, state.focusedIndex) }
        item("Copy").performKeyInput { pressKey(Key.Tab) }
        rule.onNodeWithTag("after").assertIsFocused()
    }
}
