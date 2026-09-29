package com.biat.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.biat.ui.core.state.ToggleGroupType
import com.biat.ui.core.state.rememberAvatarState
import com.biat.ui.core.state.rememberSliderState
import com.biat.ui.core.state.rememberToggleGroupState
import com.biat.ui.core.state.rememberToolbarState
import com.biat.ui.primitives.avatar.Avatar
import com.biat.ui.primitives.label.Label
import com.biat.ui.primitives.separator.Separator
import com.biat.ui.primitives.slider.Slider
import com.biat.ui.primitives.togglegroup.ToggleGroupValue
import com.biat.ui.primitives.togglegroup.ToggleGroup
import com.biat.ui.primitives.toolbar.Toolbar
import com.biat.ui.primitives.toolbar.ToolbarValue
import org.junit.Rule
import org.junit.Test

/**
 * On-device certification for v0.4.0 Bits-parity additions: slider renders
 * track + thumb with label, toggle group flips pressed membership, toolbar
 * activates items, avatar shows fallback until loaded, separator and label
 * render caller visuals.
 */
class BitsParityNewPrimitivesTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun slider_rendersTrackAndThumb() {
        rule.setContent {
            Slider(
                state = rememberSliderState(initialValue = 5f, valueRange = 0f..10f),
                label = "Volume",
                track = { BasicText("Track $it") },
                thumb = { BasicText("Thumb") },
            )
        }
        rule.onNodeWithContentDescription("Volume", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("Thumb", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun toggleGroup_clickPresses() {
        rule.setContent {
            ToggleGroup(
                state = rememberToggleGroupState(type = ToggleGroupType.Multiple),
                items = listOf(
                    ToggleGroupValue("a", "Bold"),
                    ToggleGroupValue("b", "Italic"),
                ),
                item = { entry, pressed ->
                    BasicText((if (pressed) "[x] " else "[ ] ") + entry.label)
                },
            )
        }
        rule.onNodeWithText("[ ] Bold", useUnmergedTree = true).performClick()
        rule.onNodeWithText("[x] Bold", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun toolbar_clickActivates() {
        var activated: String? = null
        rule.setContent {
            Toolbar(
                state = rememberToolbarState(itemCount = 2),
                items = listOf(
                    ToolbarValue("cut", "Cut"),
                    ToolbarValue("copy", "Copy"),
                ),
                label = "Editor",
                onActivate = { activated = it as String },
                item = { entry, _ -> BasicText(entry.label) },
            )
        }
        rule.onNodeWithText("Copy", useUnmergedTree = true).performClick()
        rule.runOnIdle { assert(activated == "copy") }
    }

    @Test
    fun avatar_showsFallbackUntilLoaded() {
        val holder = arrayOf<com.biat.ui.core.state.AvatarState?>(null)
        rule.setContent {
            val state = rememberAvatarState()
            holder[0] = state
            Avatar(
                state = state,
                label = "Profile",
                fallback = { BasicText("FB") },
                content = { BasicText("IMG") },
            )
        }
        rule.onNodeWithText("FB", useUnmergedTree = true).assertIsDisplayed()
        rule.runOnIdle { holder[0]?.markLoaded() }
        rule.onNodeWithText("IMG", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun separatorAndLabel_render() {
        rule.setContent {
            Separator { BasicText("Rule") }
            Label(controlLabel = "Name") { BasicText("Name:") }
        }
        rule.onNodeWithText("Rule", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("Name:", useUnmergedTree = true).assertIsDisplayed()
    }
}
