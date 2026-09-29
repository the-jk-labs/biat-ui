package com.biat.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.biat.ui.core.state.DialogState
import com.biat.ui.core.state.MenuState
import com.biat.ui.core.state.PopoverState
import com.biat.ui.core.state.SelectState
import com.biat.ui.core.state.SheetState
import com.biat.ui.core.state.TabsState
import com.biat.ui.primitives.dialog.Dialog
import com.biat.ui.primitives.menu.Menu
import com.biat.ui.primitives.menu.MenuItem
import com.biat.ui.primitives.popover.Popover
import com.biat.ui.primitives.select.Select
import com.biat.ui.primitives.sheet.Sheet
import com.biat.ui.primitives.tabs.TabValue
import com.biat.ui.primitives.tabs.Tabs
import com.biat.ui.primitives.tooltip.Tooltip
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * On-device keyboard-navigation certification per primitive:
 * Esc / arrows / Enter / Home / End plus focus-driven Tooltip.
 * State objects are created in the test and read back inside
 * runOnIdle; open/close is asserted through node existence.
 * Run with a connected device (no emulator needed):
 * ./gradlew :biat-ui:connectedDebugAndroidTest
 */
class KeyboardNavTest {
    @get:Rule
    val rule = createComposeRule()

    // Clickable parents merge descendant semantics, hiding test tags from
    // the merged tree. The unmerged tree contains every tagged node, so all
    // finders here use it.
    private fun node(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true)

    private fun text(value: String) = rule.onNodeWithText(value, useUnmergedTree = true)

    @Test
    fun dialog_escapeCloses() {
        val state = DialogState(initialOpen = true)
        rule.setContent {
            Dialog(state = state, dismissOnBackPress = false) {
                FocusableTag("content")
            }
        }
        node("content").assertIsFocused()
        node("content").performKeyInput { pressKey(Key.Escape) }
        node("content").assertDoesNotExist()
        rule.runOnIdle { assertEquals(false, state.isOpen) }
    }

    @Test
    fun sheet_escapeCloses() {
        val state = SheetState(initialOpen = true)
        rule.setContent {
            Sheet(state = state, dismissOnBackPress = false) {
                FocusableTag("content")
            }
        }
        node("content").assertIsFocused()
        node("content").performKeyInput { pressKey(Key.Escape) }
        node("content").assertDoesNotExist()
        rule.runOnIdle { assertEquals(false, state.isOpen) }
    }

    @Test
    fun popover_enterOpens() {
        val state = PopoverState()
        rule.setContent {
            Popover(
                state = state,
                trigger = { FocusableTag("trigger") },
            ) {
                Box(Modifier.testTag("content")) { BasicText("body") }
            }
        }
        node("trigger").assertIsFocused()
        node("content").assertDoesNotExist()
        node("trigger").performKeyInput { pressKey(Key.Enter) }
        node("content").assertExists()
        rule.runOnIdle { assertEquals(true, state.isOpen) }
    }

    @Test
    fun popover_escapeCloses() {
        val state = PopoverState(initialOpen = true)
        rule.setContent {
            Popover(
                state = state,
                dismissOnBackPress = false,
                trigger = { BasicText("trigger") },
            ) {
                FocusableTag("content")
            }
        }
        node("content").assertIsFocused()
        node("content").performKeyInput { pressKey(Key.Escape) }
        node("content").assertDoesNotExist()
        rule.runOnIdle { assertEquals(false, state.isOpen) }
    }

    @Test
    fun menu_enterOpens() {
        val state = MenuState(itemCount = 3)
        rule.setContent { MenuHarness(state = state) }
        node("trigger").assertIsFocused()
        node("menu-focus").assertDoesNotExist()
        node("trigger").performKeyInput { pressKey(Key.Enter) }
        node("menu-focus").assertExists()
        rule.runOnIdle { assertEquals(true, state.isOpen) }
    }

    @Test
    fun menu_downOpens() {
        val state = MenuState(itemCount = 3)
        rule.setContent { MenuHarness(state = state) }
        node("trigger").performKeyInput { pressKey(Key.DirectionDown) }
        node("menu-focus").assertExists()
        rule.runOnIdle { assertEquals(true, state.isOpen) }
    }

    @Test
    fun menu_arrowsCycleHighlight() {
        val state = MenuState(itemCount = 3)
        rule.setContent { MenuHarness(state = state) }
        node("trigger").performKeyInput { pressKey(Key.DirectionDown) }
        node("menu-focus").assertIsFocused()
        node("menu-focus").performKeyInput { pressKey(Key.DirectionDown) }
        rule.runOnIdle { assertEquals(0, state.highlightedIndex) }
        node("menu-focus").performKeyInput { pressKey(Key.DirectionDown) }
        rule.runOnIdle { assertEquals(1, state.highlightedIndex) }
        node("menu-focus").performKeyInput { pressKey(Key.DirectionUp) }
        rule.runOnIdle { assertEquals(0, state.highlightedIndex) }
    }

    @Test
    fun menu_homeEndJump() {
        val state = MenuState(itemCount = 3)
        rule.setContent { MenuHarness(state = state) }
        node("trigger").performKeyInput { pressKey(Key.DirectionDown) }
        node("menu-focus").assertIsFocused()
        node("menu-focus").performKeyInput { pressKey(Key.MoveEnd) }
        rule.runOnIdle { assertEquals(2, state.highlightedIndex) }
        node("menu-focus").performKeyInput { pressKey(Key.MoveHome) }
        rule.runOnIdle { assertEquals(0, state.highlightedIndex) }
    }

    @Test
    fun menu_escapeClosesAndResetsHighlight() {
        val state = MenuState(itemCount = 3)
        rule.setContent { MenuHarness(state = state) }
        node("trigger").performKeyInput { pressKey(Key.DirectionDown) }
        node("menu-focus").performKeyInput { pressKey(Key.DirectionDown) }
        rule.runOnIdle { assertEquals(0, state.highlightedIndex) }
        node("menu-focus").performKeyInput { pressKey(Key.Escape) }
        node("menu-focus").assertDoesNotExist()
        rule.runOnIdle {
            assertEquals(false, state.isOpen)
            assertEquals(-1, state.highlightedIndex)
        }
    }

    @Test
    fun select_downOpensAndArrowsHighlight() {
        val state = SelectState<String>()
        rule.setContent { SelectHarness(state = state) }
        node("trigger").assertIsFocused()
        node("trigger").performKeyInput { pressKey(Key.DirectionDown) }
        node("list-focus").assertExists()
        rule.runOnIdle { assertEquals(true, state.isOpen) }
        node("list-focus").assertIsFocused()
        node("list-focus").performKeyInput { pressKey(Key.DirectionDown) }
        rule.runOnIdle { assertEquals(0, state.highlightedIndex) }
        node("list-focus").performKeyInput { pressKey(Key.DirectionDown) }
        rule.runOnIdle { assertEquals(1, state.highlightedIndex) }
        node("list-focus").performKeyInput { pressKey(Key.DirectionUp) }
        rule.runOnIdle { assertEquals(0, state.highlightedIndex) }
    }

    @Test
    fun select_enterCommitsAndCloses() {
        val state = SelectState<String>()
        var chosen: String? = null
        rule.setContent { SelectHarness(state = state, onSelected = { chosen = it }) }
        node("trigger").performKeyInput { pressKey(Key.Enter) }
        node("list-focus").assertIsFocused()
        node("list-focus").performKeyInput { pressKey(Key.DirectionDown) }
        node("list-focus").performKeyInput { pressKey(Key.DirectionDown) }
        node("list-focus").performKeyInput { pressKey(Key.Enter) }
        node("list-focus").assertDoesNotExist()
        rule.runOnIdle {
            assertEquals("b", state.selected)
            assertEquals("b", chosen)
            assertEquals(false, state.isOpen)
        }
    }

    @Test
    fun select_escapeCloses() {
        val state = SelectState<String>()
        rule.setContent { SelectHarness(state = state) }
        node("trigger").performKeyInput { pressKey(Key.Enter) }
        node("list-focus").assertIsFocused()
        node("list-focus").performKeyInput { pressKey(Key.Escape) }
        node("list-focus").assertDoesNotExist()
        rule.runOnIdle { assertEquals(false, state.isOpen) }
    }

    @Test
    fun tabs_arrowsMoveSelection() {
        val state = TabsState(initialSelected = "a")
        rule.setContent { TabsHarness(state = state) }
        node("tab-a").assertIsFocused()
        text("A").assertExists()
        node("tab-a").performKeyInput { pressKey(Key.DirectionRight) }
        text("B").assertExists()
        node("tab-a").performKeyInput { pressKey(Key.DirectionRight) }
        text("C").assertExists()
        node("tab-a").performKeyInput { pressKey(Key.DirectionRight) }
        text("A").assertExists()
        node("tab-a").performKeyInput { pressKey(Key.DirectionLeft) }
        text("C").assertExists()
    }

    @Test
    fun tabs_homeEndJump() {
        val state = TabsState(initialSelected = "b")
        rule.setContent { TabsHarness(state = state) }
        node("tab-a").assertIsFocused()
        text("B").assertExists()
        node("tab-a").performKeyInput { pressKey(Key.MoveEnd) }
        text("C").assertExists()
        node("tab-a").performKeyInput { pressKey(Key.MoveHome) }
        text("A").assertExists()
    }

    @Test
    fun tooltip_focusShowsOverlay() {
        rule.setContent {
            Tooltip(
                tip = "tip",
                trigger = { FocusableTag("anchor") },
                content = {
                    Box(Modifier.testTag("tip")) { BasicText("tip") }
                },
            )
        }
        node("anchor").assertIsFocused()
        node("tip").assertExists()
    }
}

/** Focusable tagged node that grabs focus on launch when [request]. */
@Composable
private fun FocusableTag(
    tag: String,
    request: Boolean = true,
) {
    val requester = remember { FocusRequester() }
    if (request) {
        LaunchedEffect(Unit) { requester.requestFocus() }
    }
    Box(
        Modifier
            .focusRequester(requester)
            .focusable()
            .testTag(tag),
    ) {
        BasicText(tag)
    }
}

@Composable
private fun MenuHarness(state: MenuState) {
    Menu(
        state = state,
        trigger = { FocusableTag("trigger", request = !state.isOpen) },
    ) {
        FocusableTag("menu-focus")
        listOf("a", "b", "c").forEach { item ->
            MenuItem(state = state, label = item, onSelect = {}) {
                BasicText(item)
            }
        }
    }
}

@Composable
private fun SelectHarness(
    state: SelectState<String>,
    onSelected: ((String?) -> Unit)? = null,
) {
    Select(
        state = state,
        options = listOf("a", "b", "c"),
        trigger = { FocusableTag("trigger", request = !state.isOpen) },
        item = { value, _, _ ->
            if (value == "a") {
                FocusableTag("list-focus")
            }
            BasicText(value)
        },
        onSelected = onSelected,
    )
}

@Composable
private fun TabsHarness(state: TabsState) {
    val tabs =
        listOf(
            TabValue("a", "A"),
            TabValue("b", "B"),
            TabValue("c", "C"),
        )
    Tabs(
        state = state,
        tabs = tabs,
        tab = { item, _, _ ->
            FocusableTag("tab-${item.value}", request = item.value == "a")
        },
        panel = { selected ->
            Box(Modifier.testTag("panel")) {
                BasicText(selected?.label ?: "none")
            }
        },
    )
}
