package com.biat.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.biat.ui.core.focus.focusTrap
import com.biat.ui.core.focus.rememberFocusTrapRequester
import com.biat.ui.core.focus.rememberFocusTrapState
import org.junit.Rule
import org.junit.Test

/**
 * On-device tests for [com.biat.ui.core.focus.focusTrap].
 * Run with a connected device (no emulator needed):
 * ./gradlew :biat-ui:connectedDebugAndroidTest
 */
class FocusTrapTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun tab_cyclesForwardAndWrapsToFirst() {
        rule.setContent { TrapHarness() }
        rule.onNodeWithTag("item-0").assertIsFocused()

        rule.onNodeWithTag("item-0").performKeyInput { pressKey(Key.Tab) }
        rule.onNodeWithTag("item-1").assertIsFocused()

        rule.onNodeWithTag("item-1").performKeyInput { pressKey(Key.Tab) }
        rule.onNodeWithTag("item-2").assertIsFocused()

        rule.onNodeWithTag("item-2").performKeyInput { pressKey(Key.Tab) }
        rule.onNodeWithTag("item-0").assertIsFocused()
        rule.onNodeWithTag("outside").assertIsNotFocused()
    }

    @Test
    fun shiftTab_cyclesBackwardAndWrapsToLast() {
        rule.setContent { TrapHarness() }
        rule.onNodeWithTag("item-0").assertIsFocused()

        rule.onNodeWithTag("item-0").performKeyInput {
            keyDown(Key.ShiftLeft)
            pressKey(Key.Tab)
            keyUp(Key.ShiftLeft)
        }
        rule.onNodeWithTag("item-2").assertIsFocused()
        rule.onNodeWithTag("outside").assertIsNotFocused()

        rule.onNodeWithTag("item-2").performKeyInput {
            keyDown(Key.ShiftLeft)
            pressKey(Key.Tab)
            keyUp(Key.ShiftLeft)
        }
        rule.onNodeWithTag("item-1").assertIsFocused()
    }

    @Test
    fun escape_forwardsToOnEscape() {
        var escaped = false
        rule.setContent {
            TrapHarness(onEscape = { escaped = true })
        }
        rule.onNodeWithTag("item-0").assertIsFocused()
        rule.onNodeWithTag("item-0").performKeyInput { pressKey(Key.Escape) }
        rule.runOnIdle { assert(escaped) { "onEscape was not invoked" } }
    }
}

@Composable
private fun TrapHarness(onEscape: (() -> Unit)? = null) {
    val trapRequester = rememberFocusTrapRequester()
    val trapState = rememberFocusTrapState()
    val focusManager = LocalFocusManager.current
    val firstRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { firstRequester.requestFocus() }

    Column {
        Box(
            Modifier.focusTrap(
                active = true,
                trapRequester = trapRequester,
                focusManager = focusManager,
                trapState = trapState,
                onEscape = onEscape,
            ),
        ) {
            Column {
                repeat(3) { index ->
                    Box(
                        Modifier
                            .then(if (index == 0) Modifier.focusRequester(firstRequester) else Modifier)
                            .focusable()
                            .testTag("item-$index"),
                    ) {
                        BasicText("item $index")
                    }
                }
            }
        }
        Box(
            Modifier
                .focusable()
                .testTag("outside"),
        ) {
            BasicText("outside")
        }
    }
}
