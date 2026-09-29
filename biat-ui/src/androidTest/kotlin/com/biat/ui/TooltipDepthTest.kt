package com.biat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.biat.ui.core.positioning.PopupSide
import com.biat.ui.core.state.TooltipState
import com.biat.ui.core.state.rememberTooltipState
import com.biat.ui.primitives.tooltip.Tooltip
import org.junit.Rule
import org.junit.Test

/**
 * On-device certification for Tooltip depth (v0.3.0): touch long-press
 * trigger with opt-out, and every placement side rendering through the
 * shared collision engine (flip/clamp math itself is JVM-pinned in
 * PositioningTest; popup windows have no common frame with the anchor,
 * so exact offsets are not assertable here).
 */
class TooltipDepthTest {
    @get:Rule
    val rule = createComposeRule()

    private fun node(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true)

    @Test
    fun longPress_showsOverlay() {
        rule.setContent { TooltipDepthHarness() }
        node("ttip").assertDoesNotExist()
        node("tanchor").performTouchInput { longClick() }
        node("ttip").assertExists()
    }

    @Test
    fun longPress_disabled_ignoresGesture() {
        rule.setContent { TooltipDepthHarness(enableLongPress = false) }
        node("tanchor").performTouchInput { longClick() }
        node("ttip").assertDoesNotExist()
    }

    @Test
    fun placement_eachSide_rendersOverlay() {
        val state = TooltipState()
        val side = mutableStateOf(PopupSide.Top)
        rule.setContent { TooltipDepthHarness(state = state, side = side) }
        PopupSide.entries.forEach { next ->
            rule.runOnIdle {
                state.hide()
                side.value = next
            }
            node("tanchor").performTouchInput { longClick() }
            node("ttip").assertExists()
        }
    }
}

@Composable
private fun TooltipDepthHarness(
    state: TooltipState = rememberTooltipState(),
    side: State<PopupSide> = remember { mutableStateOf(PopupSide.Top) },
    enableLongPress: Boolean = true,
) {
    Column {
        Spacer(Modifier.height(200.dp))
        Tooltip(
            state = state,
            tip = "depth tip",
            side = side.value,
            enableLongPress = enableLongPress,
            content = {
                Box(
                    Modifier
                        .testTag("ttip")
                        .fillMaxWidth()
                        .height(40.dp),
                ) {
                    BasicText("tip")
                }
            },
            trigger = {
                Box(Modifier.testTag("tanchor")) {
                    BasicText("anchor")
                }
            },
        )
    }
}
