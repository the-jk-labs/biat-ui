package com.biat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.biat.ui.core.dismiss.consumeOverlayTaps
import com.biat.ui.core.dismiss.outsideClick
import com.biat.ui.core.state.rememberDialogState
import com.biat.ui.core.state.rememberSheetState
import com.biat.ui.primitives.dialog.Dialog
import com.biat.ui.primitives.sheet.Sheet
import org.junit.Rule
import org.junit.Test

/**
 * On-device tests proving scrim taps dismiss while content taps do not.
 * Run with a connected device (no emulator needed):
 * ./gradlew :biat-ui:connectedDebugAndroidTest
 */
class DismissTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun scrimTap_dismisses() {
        rule.setContent { DismissHarness() }
        rule.onNodeWithTag("content").assertExists()
        // 10% width / 50% height: scrim only, content is centered.
        clickScrim()
        rule.onAllNodesWithTag("content").assertCountEquals(0)
    }

    @Test
    fun contentTap_doesNotDismiss() {
        rule.setContent { DismissHarness() }
        rule.onNodeWithTag("content").assertExists()
        rule.onNodeWithTag("content").performTouchInput { click() }
        rule.onNodeWithTag("content").assertExists()
    }

    @Test
    fun dialog_scrimTap_dismisses() {
        rule.setContent { DialogHarness() }
        rule.onNodeWithTag("content").assertExists()
        // Gutter point inside the dialog window but outside centered content.
        // The platform dialog window wraps content, so absolute corners can
        // fall outside the window entirely.
        clickScrim()
        rule.onAllNodesWithTag("content").assertCountEquals(0)
    }

    @Test
    fun dialog_contentTap_doesNotDismiss() {
        rule.setContent { DialogHarness() }
        rule.onNodeWithTag("content").assertExists()
        rule.onNodeWithTag("content").performTouchInput { click() }
        rule.onNodeWithTag("content").assertExists()
    }

    @Test
    fun sheet_scrimTap_dismisses() {
        rule.setContent { SheetHarness() }
        rule.onNodeWithTag("content").assertExists()
        clickScrim()
        rule.onAllNodesWithTag("content").assertCountEquals(0)
    }

    @Test
    fun sheet_contentTap_doesNotDismiss() {
        rule.setContent { SheetHarness() }
        rule.onNodeWithTag("content").assertExists()
        rule.onNodeWithTag("content").performTouchInput { click() }
        rule.onNodeWithTag("content").assertExists()
    }

    /**
     * Clicks the scrim node at 10% width / 50% height: inside the scrim,
     * outside centered content, independent of screen size.
     */
    private fun clickScrim() {
        val size = rule.onNodeWithTag("scrim").fetchSemanticsNode().size
        rule.onNodeWithTag("scrim").performTouchInput {
            click(Offset(size.width * 0.1f, size.height * 0.5f))
        }
    }
}

@Composable
private fun DismissHarness() {
    var open by remember { mutableStateOf(true) }
    Box(
        Modifier
            .fillMaxSize()
            .testTag("scrim")
            .outsideClick(onOutsideClick = { open = false }),
    ) {
        if (open) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(200.dp)
                    .consumeOverlayTaps()
                    .testTag("content"),
            ) {
                BasicText("content")
            }
        }
    }
}

@Composable
private fun SheetHarness() {
    val state = rememberSheetState(initialOpen = true)
    Sheet(
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
                .fillMaxWidth()
                .testTag("content"),
        ) {
            BasicText("sheet content")
        }
    }
}

@Composable
private fun DialogHarness() {
    val state = rememberDialogState(initialOpen = true)
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
        // Wrap-content card: its bounds are the dismiss boundary.
        // (fillMaxSize content would claim the whole window.)
        Box(
            Modifier
                .size(200.dp)
                .testTag("content"),
        ) {
            BasicText("content")
        }
    }
}
