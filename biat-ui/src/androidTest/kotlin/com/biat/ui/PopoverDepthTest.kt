package com.biat.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import com.biat.ui.core.state.PopoverState
import com.biat.ui.primitives.popover.Popover
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * On-device certification for Popover depth (v0.3.0): modal focus trap
 * with entry focus plus Escape dismissal, and scroll integrity (open
 * popover survives anchor scroll with state intact).
 */
class PopoverDepthTest {
    @get:Rule
    val rule = createComposeRule()

    private fun node(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true)

    @Test
    fun modal_tabWrapsInside() {
        val state = PopoverState()
        rule.setContent { PopoverDepthHarness(state = state, modal = true) }
        rule.runOnIdle { state.open() }
        // Tab rides in through the plain content node so every press tunnels
        // through the trap preview; assertions read the focusable items.
        // Entry focus landed in the popup: first Tab reaches item A.
        node("pcontent").performKeyInput { pressKey(Key.Tab) }
        node("pitem-a").assertIsFocused()
        node("pcontent").performKeyInput { pressKey(Key.Tab) }
        node("pitem-b").assertIsFocused()
        // Third Tab wraps around to the first item (focus is zero-sum, so
        // the outside trigger cannot hold it while an item is focused).
        node("pcontent").performKeyInput { pressKey(Key.Tab) }
        node("pitem-a").assertIsFocused()
        // Cycling continues after the wrap.
        node("pcontent").performKeyInput { pressKey(Key.Tab) }
        node("pitem-b").assertIsFocused()
    }

    @Test
    fun modal_escapeDismisses() {
        val state = PopoverState()
        rule.setContent { PopoverDepthHarness(state = state, modal = true) }
        rule.runOnIdle { state.open() }
        node("pcontent").assertExists()
        // Escape must ride on the focused item: popup-window injection only
        // tunnels when target and focus coincide.
        node("pcontent").performKeyInput { pressKey(Key.Tab) }
        node("pitem-a").assertIsFocused()
        node("pitem-a").performKeyInput { pressKey(Key.Escape) }
        node("pcontent").assertDoesNotExist()
    }

    @Test
    fun scrollAnchor_survivesAndStaysFunctional() {
        lateinit var scroll: ScrollState
        lateinit var scope: CoroutineScope
        val state = PopoverState()
        rule.setContent {
            scroll = rememberScrollState()
            scope = rememberCoroutineScope()
            PopoverDepthHarness(state = state, scroll = scroll)
        }
        rule.runOnIdle { state.open() }
        node("pcontent").assertExists()
        rule.runOnIdle { scope.launch { scroll.scrollTo(600) } }
        // Still open with content intact after the anchor moved, and the
        // state still drives the popup. (Pixel tracking itself is verified
        // against fresh anchor bounds by the platform + engine; popup
        // windows share no assertable frame with the anchor in this setup.)
        rule.runOnIdle { assertTrue(state.isOpen) }
        node("pcontent").assertExists()
        rule.runOnIdle { state.close() }
        node("pcontent").assertDoesNotExist()
    }
}

@Composable
private fun PopoverDepthHarness(
    state: PopoverState,
    modal: Boolean = false,
    scroll: ScrollState? = null,
) {
    Column(
        modifier =
            if (scroll != null) {
                Modifier.verticalScroll(scroll)
            } else {
                Modifier
            },
    ) {
        Popover(
            state = state,
            modal = modal,
            trigger = {
                Box(Modifier.testTag("ptrigger")) {
                    BasicText("trigger")
                }
            },
            content = {
                Box(Modifier.testTag("pcontent")) {
                    Column {
                        Box(
                            Modifier
                                .focusable()
                                .testTag("pitem-a"),
                        ) {
                            BasicText("item a")
                        }
                        Box(
                            Modifier
                                .focusable()
                                .testTag("pitem-b"),
                        ) {
                            BasicText("item b")
                        }
                    }
                }
            },
        )
        Box(Modifier.fillMaxWidth()) {
            BasicText("below")
        }
        Spacer(Modifier.height(2000.dp))
    }
}
