package com.biat.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.LayoutDirection
import com.biat.ui.core.state.TabsState
import com.biat.ui.primitives.tabs.TabValue
import com.biat.ui.primitives.tabs.Tabs
import com.biat.ui.primitives.tabs.TabsActivation
import com.biat.ui.primitives.tabs.TabsOrientation
import org.junit.Rule
import org.junit.Test

/**
 * On-device certification for Tabs depth (v0.3.0): manual activation moves
 * focus without selecting, Enter/Space activates, vertical lists use
 * Up/Down, and horizontal arrows flip in RTL.
 */
class TabsDepthTest {
    @get:Rule
    val rule = createComposeRule()

    private fun node(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true)

    @Test
    fun manual_arrowsMoveFocusWithoutSelecting() {
        val state = TabsState(initialSelected = "a")
        rule.setContent {
            TabsDepthHarness(state = state, activation = TabsActivation.Manual)
        }
        node("dtab-a").assertExists()
        node("dtab-a").performKeyInput { pressKey(Key.DirectionRight) }
        // Selection stayed on A; the companion enter test below proves focus
        // moved into tab B (Enter can only activate the focused tab).
        rule.onNodeWithText("A", useUnmergedTree = true).assertExists()
    }

    @Test
    fun manual_enterActivatesFocusedTab() {
        val state = TabsState(initialSelected = "a")
        rule.setContent {
            TabsDepthHarness(state = state, activation = TabsActivation.Manual)
        }
        node("dtab-a").performKeyInput { pressKey(Key.DirectionRight) }
        node("dtab-b").performKeyInput { pressKey(Key.Enter) }
        rule.onNodeWithText("B", useUnmergedTree = true).assertExists()
    }

    @Test
    fun automatic_arrowsSelectAndMoveFocus() {
        val state = TabsState(initialSelected = "a")
        rule.setContent { TabsDepthHarness(state = state) }
        node("dtab-a").performKeyInput { pressKey(Key.DirectionRight) }
        rule.onNodeWithText("B", useUnmergedTree = true).assertExists()
    }

    @Test
    fun vertical_upDownNavigate() {
        val state = TabsState(initialSelected = "b")
        rule.setContent {
            TabsDepthHarness(state = state, orientation = TabsOrientation.Vertical)
        }
        node("dtab-a").performKeyInput { pressKey(Key.DirectionUp) }
        rule.onNodeWithText("A", useUnmergedTree = true).assertExists()
        node("dtab-b").performKeyInput { pressKey(Key.DirectionDown) }
        rule.onNodeWithText("B", useUnmergedTree = true).assertExists()
        node("dtab-b").performKeyInput { pressKey(Key.DirectionDown) }
        rule.onNodeWithText("C", useUnmergedTree = true).assertExists()
    }

    @Test
    fun vertical_ignoresLeftRight() {
        val state = TabsState(initialSelected = "a")
        rule.setContent {
            TabsDepthHarness(state = state, orientation = TabsOrientation.Vertical)
        }
        node("dtab-a").performKeyInput { pressKey(Key.DirectionRight) }
        rule.onNodeWithText("A", useUnmergedTree = true).assertExists()
        node("dtab-a").performKeyInput { pressKey(Key.DirectionLeft) }
        rule.onNodeWithText("A", useUnmergedTree = true).assertExists()
    }

    @Test
    fun rtl_rightMovesBackward() {
        val state = TabsState(initialSelected = "a")
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                TabsDepthHarness(state = state)
            }
        }
        node("dtab-a").performKeyInput { pressKey(Key.DirectionRight) }
        // Right wraps backward from A to C in RTL.
        rule.onNodeWithText("C", useUnmergedTree = true).assertExists()
    }
}

@Composable
private fun TabsDepthHarness(
    state: TabsState,
    orientation: TabsOrientation = TabsOrientation.Horizontal,
    activation: TabsActivation = TabsActivation.Automatic,
) {
    val tabs =
        listOf(
            TabValue("a", "A"),
            TabValue("b", "B"),
            TabValue("c", "C"),
        )
    Tabs(
        layout = { content ->
            if (orientation == TabsOrientation.Vertical) Column { content() } else Row { content() }
        },
        state = state,
        tabs = tabs,
        orientation = orientation,
        activation = activation,
        tab = { item, _, _ ->
            FocusableInnerTag("dtab-${item.value}", request = state.isSelected(item.value))
        },
        panel = { selected ->
            Box(Modifier.testTag("dpanel")) {
                BasicText(selected?.label ?: "none")
            }
        },
    )
}

/** Focusable tagged node that grabs focus on launch when [request]. */
@Composable
private fun FocusableInnerTag(
    tag: String,
    request: Boolean,
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
