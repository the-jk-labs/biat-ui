package com.biat.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import com.biat.ui.core.state.MenuState
import com.biat.ui.core.state.rememberMenuState
import com.biat.ui.primitives.menu.Menu
import com.biat.ui.primitives.menu.MenuCheckboxItem
import com.biat.ui.primitives.menu.MenuItem
import com.biat.ui.primitives.menu.MenuRadioItem
import com.biat.ui.primitives.menu.MenuSeparator
import com.biat.ui.primitives.menu.MenuSub
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * On-device certification for Menu depth (v0.3.0): disabled items,
 * separators, checkbox persistence, radio close, submenu layering.
 */
class MenuDepthTest {
    @get:Rule
    val rule = createComposeRule()

    private fun node(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true)

    private fun text(value: String) = rule.onNodeWithText(value, useUnmergedTree = true)

    @Test
    fun disabledItem_hasNoClickAction_enabledCloses() {
        var chosen = "none"
        val state = MenuState(itemCount = 2, disabledIndices = setOf(1))
        rule.setContent {
            Menu(
                state = state,
                trigger = { BasicText("trigger") },
            ) {
                MenuItem(state = state, label = "Edit", onSelect = { chosen = "Edit" }) {
                    BasicText("Edit")
                }
                MenuItem(
                    state = state,
                    enabled = false,
                    label = "Delete",
                    onSelect = { chosen = "Delete" },
                ) {
                    BasicText("Delete")
                }
            }
        }
        rule.runOnIdle { state.open() }
        // Disabled items expose no activation: TalkBack offers nothing,
        // touch has nowhere to land, keyboard skips the index (unit-tested).
        val disabledConfig =
            rule
                .onNodeWithContentDescription(
                    "Delete",
                    useUnmergedTree = true,
                ).fetchSemanticsNode()
                .config
        val enabledConfig =
            rule
                .onNodeWithContentDescription(
                    "Edit",
                    useUnmergedTree = true,
                ).fetchSemanticsNode()
                .config
        rule.runOnIdle {
            assertFalse(disabledConfig.contains(SemanticsActions.OnClick))
            assertTrue(disabledConfig.contains(SemanticsProperties.Disabled))
            assertTrue(enabledConfig.contains(SemanticsActions.OnClick))
            assertEquals("none", chosen)
            assertTrue(state.isOpen)
        }
        rule
            .onNodeWithContentDescription(
                "Edit",
                useUnmergedTree = true,
            ).performTouchInput { click() }
        rule.runOnIdle {
            assertEquals("Edit", chosen)
            assertFalse(state.isOpen)
        }
    }

    @Test
    fun checkbox_togglesAndStaysOpen() {
        val checked = mutableStateOf(false)
        val state = MenuState(itemCount = 1)
        rule.setContent {
            Menu(
                state = state,
                trigger = { BasicText("trigger") },
            ) {
                MenuCheckboxItem(
                    checked = checked.value,
                    onCheckedChange = { checked.value = it },
                    label = "Show hidden",
                ) {
                    BasicText("Show hidden")
                }
            }
        }
        rule.runOnIdle { state.open() }
        text("Show hidden").performTouchInput { click() }
        rule.runOnIdle {
            assertTrue(checked.value)
            assertTrue(state.isOpen)
        }
        text("Show hidden").performTouchInput { click() }
        rule.runOnIdle {
            assertFalse(checked.value)
            assertTrue(state.isOpen)
        }
    }

    @Test
    fun radio_selectsAndCloses() {
        var sort = "Name"
        val state = MenuState(itemCount = 2)
        rule.setContent {
            Menu(
                state = state,
                trigger = { BasicText("trigger") },
            ) {
                listOf("Name", "Date").forEach { value ->
                    MenuRadioItem(
                        state = state,
                        selected = sort == value,
                        onSelect = { sort = value },
                        label = value,
                    ) {
                        BasicText(value)
                    }
                }
            }
        }
        rule.runOnIdle { state.open() }
        text("Date").performTouchInput { click() }
        rule.runOnIdle {
            assertEquals("Date", sort)
            assertFalse(state.isOpen)
        }
    }

    @Test
    fun separator_rendersWithoutAffectingItems() {
        val state = MenuState(itemCount = 1)
        rule.setContent {
            Menu(
                state = state,
                trigger = { BasicText("trigger") },
            ) {
                MenuItem(state = state, onSelect = {}) { BasicText("Edit") }
                MenuSeparator { BasicText("divider") }
            }
        }
        rule.runOnIdle { state.open() }
        text("divider").assertExists()
        rule.runOnIdle { state.moveHighlight(1) }
        rule.runOnIdle { assertEquals(0, state.highlightedIndex) }
    }

    @Test
    fun submenu_touchOpens_escapeClosesOnlySubmenu() {
        val parent = MenuState(itemCount = 2)
        val sub = MenuState(itemCount = 1)
        rule.setContent {
            Menu(
                state = parent,
                trigger = { BasicText("trigger") },
            ) {
                MenuItem(state = parent, onSelect = {}) { BasicText("Edit") }
                MenuSub(
                    state = sub,
                    label = "Share",
                    trigger = { BasicText("Share") },
                ) {
                    FocusableTag("sub-focus")
                    MenuItem(state = sub, onSelect = {}) { BasicText("Copy link") }
                }
            }
        }
        rule.runOnIdle { parent.open() }
        text("Share").performTouchInput { click() }
        text("Copy link").assertExists()
        node("sub-focus").performKeyInput { pressKey(Key.Escape) }
        text("Copy link").assertDoesNotExist()
        rule.runOnIdle {
            assertFalse(sub.isOpen)
            assertTrue(parent.isOpen)
        }
        text("Edit").assertExists()
    }

    @Test
    fun submenu_arrowRightOpensFromTrigger() {
        val parent = MenuState(itemCount = 1)
        val sub = MenuState(itemCount = 1)
        rule.setContent {
            Menu(
                state = parent,
                trigger = { BasicText("trigger") },
            ) {
                MenuSub(
                    state = sub,
                    label = "Share",
                    trigger = { FocusableTag("sub-trigger") },
                ) {
                    MenuItem(state = sub, onSelect = {}) { BasicText("Copy link") }
                }
            }
        }
        rule.runOnIdle { parent.open() }
        node("sub-trigger").performKeyInput { pressKey(Key.DirectionRight) }
        text("Copy link").assertExists()
        rule.runOnIdle { assertTrue(sub.isOpen) }
    }
}

/** Focusable tagged node that grabs focus on launch. */
@Composable
private fun FocusableTag(tag: String) {
    val requester = remember { FocusRequester() }
    LaunchedEffect(Unit) { requester.requestFocus() }
    Box(
        Modifier
            .focusRequester(requester)
            .focusable()
            .testTag(tag),
    ) {
        BasicText(tag)
    }
}
