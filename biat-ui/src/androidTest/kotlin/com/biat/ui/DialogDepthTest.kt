package com.biat.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import com.biat.ui.core.state.DialogState
import com.biat.ui.primitives.dialog.Dialog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * On-device certification for Dialog depth (v0.3.0): nested Esc layering,
 * alert outside-tap immunity, initial-focus target.
 */
class DialogDepthTest {

    @get:Rule
    val rule = createComposeRule()

    private fun node(tag: String) =
        rule.onNodeWithTag(tag, useUnmergedTree = true)

    private fun text(value: String) =
        rule.onNodeWithText(value, useUnmergedTree = true)

    @Test
    fun nested_escClosesInnerOnlyThenOuter() {
        val outer = DialogState()
        val inner = DialogState()
        rule.setContent {
            Dialog(
                state = outer,
                trigger = { BasicText("outer-trigger") },
            ) {
                // Refocuses whenever the inner dialog closes.
                if (!inner.isOpen) FocusableTag("outer-focus")
                BasicText("outer-body")
                Dialog(
                    state = inner,
                    trigger = { BasicText("inner-trigger") },
                ) {
                    FocusableTag("inner-focus")
                    BasicText("inner-body")
                }
            }
        }
        rule.runOnIdle { outer.open() }
        rule.runOnIdle { inner.open() }
        text("inner-body").assertExists()
        node("inner-focus").performKeyInput { pressKey(Key.Escape) }
        text("inner-body").assertDoesNotExist()
        rule.runOnIdle {
            assertFalse(inner.isOpen)
            assertTrue(outer.isOpen)
        }
        text("outer-body").assertExists()
        node("outer-focus").performKeyInput { pressKey(Key.Escape) }
        text("outer-body").assertDoesNotExist()
        rule.runOnIdle { assertFalse(outer.isOpen) }
    }

    @Test
    fun alert_scrimTap_doesNotDismiss_explicitCloseDoes() {
        val state = DialogState()
        rule.setContent {
            Dialog(
                state = state,
                isAlert = true,
                dismissOnEscape = false,
                dismissOnBackPress = false,
                scrim = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .testTag("scrim"),
                    )
                },
            ) {
                Box(
                    Modifier
                        .size(200.dp)
                        .testTag("content"),
                ) {
                    BasicText("alert-body")
                }
            }
        }
        rule.runOnIdle { state.open() }
        node("content").assertExists()
        val size = rule.onNodeWithTag("scrim").fetchSemanticsNode().size
        rule.onNodeWithTag("scrim").performTouchInput {
            click(Offset(size.width * 0.1f, size.height * 0.5f))
        }
        node("content").assertExists()
        rule.runOnIdle { assertTrue(state.isOpen) }
        rule.runOnIdle { state.close() }
        node("content").assertDoesNotExist()
    }

    @Test
    fun plain_scrimTap_dismissesByDefault() {
        val state = DialogState()
        rule.setContent {
            Dialog(
                state = state,
                dismissOnEscape = false,
                dismissOnBackPress = false,
                scrim = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .testTag("scrim"),
                    )
                },
            ) {
                Box(
                    Modifier
                        .size(200.dp)
                        .testTag("content"),
                ) {
                    BasicText("plain-body")
                }
            }
        }
        rule.runOnIdle { state.open() }
        node("content").assertExists()
        val size = rule.onNodeWithTag("scrim").fetchSemanticsNode().size
        rule.onNodeWithTag("scrim").performTouchInput {
            click(Offset(size.width * 0.1f, size.height * 0.5f))
        }
        node("content").assertDoesNotExist()
        rule.runOnIdle { assertFalse(state.isOpen) }
    }

    @Test
    fun initialFocus_landsOnTarget() {
        val state = DialogState()
        val target = FocusRequester()
        rule.setContent {
            Dialog(
                state = state,
                initialFocusRequester = target,
                trigger = { BasicText("trigger") },
            ) {
                BasicText("first-line")
                Box(
                    Modifier
                        .focusRequester(target)
                        .focusable()
                        .testTag("target"),
                ) {
                    BasicText("confirm")
                }
            }
        }
        rule.runOnIdle { state.open() }
        node("target").assertIsFocused()
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
