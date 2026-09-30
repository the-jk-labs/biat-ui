package com.biat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import com.biat.ui.core.state.DialogState
import com.biat.ui.core.state.MenuState
import com.biat.ui.core.state.SelectState
import com.biat.ui.core.state.SheetState
import com.biat.ui.core.state.TabsState
import com.biat.ui.primitives.dialog.Dialog
import com.biat.ui.primitives.menu.Menu
import com.biat.ui.primitives.menu.MenuItem
import com.biat.ui.primitives.select.Select
import com.biat.ui.primitives.sheet.Sheet
import com.biat.ui.primitives.tabs.TabValue
import com.biat.ui.primitives.tabs.Tabs
import com.biat.ui.primitives.tooltip.Tooltip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * On-device screen-reader certification: labels reach the semantics tree
 * with the roles and selection state TalkBack announces. Run with a
 * connected device (no emulator needed):
 * ./gradlew :biat-ui:connectedDebugAndroidTest
 */
class SemanticsTest {
    @get:Rule
    val rule = createComposeRule()

    // Roles and descriptions live on merged parents (clickable wrappers),
    // not on tagged children. Walk the unmerged tree to find the node that
    // carries them, which is what TalkBack announces.
    private fun findNode(
        role: Role? = null,
        contentDescription: String? = null,
    ): SemanticsNode {
        val queue = ArrayDeque<SemanticsNode>()
        queue.add(rule.onRoot(useUnmergedTree = true).fetchSemanticsNode())
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val currentRole =
                if (current.config.contains(SemanticsProperties.Role)) {
                    current.config[SemanticsProperties.Role]
                } else {
                    null
                }
            val descriptions =
                if (
                    current.config.contains(SemanticsProperties.ContentDescription)
                ) {
                    current.config[SemanticsProperties.ContentDescription]
                } else {
                    null
                } ?: emptyList()
            if ((role == null || currentRole == role) &&
                (contentDescription == null || descriptions.contains(contentDescription))
            ) {
                return current
            }
            queue.addAll(current.children)
        }
        throw AssertionError("no node role=$role description=$contentDescription")
    }

    @Test
    fun dialog_labelExposed() {
        rule.setContent {
            Dialog(state = DialogState(initialOpen = true), label = "Confirm delete") {
                BasicText("body")
            }
        }
        rule.onNodeWithContentDescription("Confirm delete").assertExists()
    }

    @Test
    fun sheet_labelExposed() {
        rule.setContent {
            Sheet(state = SheetState(initialOpen = true), label = "Actions sheet") {
                BasicText("body")
            }
        }
        rule.onNodeWithContentDescription("Actions sheet").assertExists()
    }

    @Test
    fun menu_labelExposed() {
        val state = MenuState(itemCount = 1, initialOpen = true)
        rule.setContent {
            Menu(
                state = state,
                label = "Actions",
                trigger = { BasicText("open") },
            ) {
                MenuItem(state = state, label = "Edit", onSelect = {}) {
                    BasicText("Edit")
                }
            }
        }
        rule.onAllNodesWithContentDescription("Actions").assertCountEquals(2)
    }

    @Test
    fun tabs_listLabelExposed() {
        rule.setContent {
            TabsHarness(label = "Settings sections")
        }
        rule.onNodeWithContentDescription("Settings sections").assertExists()
    }

    @Test
    fun menuItem_roleButtonAndSelection() {
        val state = MenuState()
        rule.setContent {
            MenuItem(state = state, label = "Edit", selected = true, onSelect = {}) {
                Box(Modifier.testTag("item-on")) { BasicText("Edit") }
            }
            MenuItem(state = state, label = "Delete", selected = false, onSelect = {}) {
                Box(Modifier.testTag("item-off")) { BasicText("Delete") }
            }
        }
        val on = findNode(Role.Button, "Edit").config
        val off = findNode(Role.Button, "Delete").config
        rule.runOnIdle {
            assertEquals(Role.Button, on[SemanticsProperties.Role])
            assertEquals(true, on[SemanticsProperties.Selected])
            assertTrue(
                on[SemanticsProperties.ContentDescription]
                    ?.contains("Edit") == true,
            )
            assertEquals(Role.Button, off[SemanticsProperties.Role])
            assertEquals(false, off[SemanticsProperties.Selected])
        }
    }

    @Test
    fun tabs_tabRoleAndSelection() {
        rule.setContent {
            TabsHarness(label = null)
        }
        val selected = findNode(Role.Tab, "A").config
        val other = findNode(Role.Tab, "B").config
        rule.runOnIdle {
            assertEquals(Role.Tab, selected[SemanticsProperties.Role])
            assertEquals(true, selected[SemanticsProperties.Selected])
            assertEquals(Role.Tab, other[SemanticsProperties.Role])
            assertEquals(false, other[SemanticsProperties.Selected])
        }
    }

    @Test
    fun tooltip_anchorAnnouncesTip() {
        rule.setContent {
            Tooltip(
                tip = "Save changes",
                trigger = {
                    Box(Modifier.testTag("anchor")) { BasicText("Save") }
                },
                content = { BasicText("Save changes") },
            )
        }
        val tipConfig = findNode(contentDescription = "Save changes").config
        rule.runOnIdle {
            assertTrue(
                tipConfig[SemanticsProperties.ContentDescription]
                    ?.contains("Save changes") == true,
            )
            assertEquals(
                "Save changes",
                tipConfig[SemanticsProperties.StateDescription],
            )
        }
    }

    @Test
    fun select_triggerDropdownRole() {
        rule.setContent {
            Select(
                state = SelectState<String>(),
                options = listOf("a", "b"),
                trigger = {
                    Box(Modifier.testTag("trigger")) { BasicText("pick") }
                },
                item = { value, _, _ -> BasicText(value) },
            )
        }
        val triggerConfig = findNode(Role.DropdownList).config
        rule.runOnIdle {
            assertEquals(
                Role.DropdownList,
                triggerConfig[SemanticsProperties.Role],
            )
        }
    }
}

@Composable
private fun TabsHarness(label: String?) {
    val tabs =
        listOf(
            TabValue("a", "A"),
            TabValue("b", "B"),
        )
    Tabs(
        state = TabsState(initialSelected = "a"),
        tabs = tabs,
        label = label,
        tab = { item, _, _ ->
            Box(Modifier.testTag("tab-${item.value}")) {
                BasicText(item.label)
            }
        },
        panel = {},
    )
}
