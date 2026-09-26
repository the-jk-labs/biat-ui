package com.biat.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.biat.ui.core.state.rememberDialogState
import com.biat.ui.core.state.rememberMenuState
import com.biat.ui.core.state.rememberPopoverState
import com.biat.ui.core.state.rememberSelectState
import com.biat.ui.core.state.rememberSheetState
import com.biat.ui.core.state.rememberTabsState
import com.biat.ui.core.state.rememberTooltipState
import com.biat.ui.primitives.dialog.Dialog
import com.biat.ui.primitives.dialog.DialogClose
import com.biat.ui.primitives.menu.Menu
import com.biat.ui.primitives.menu.MenuItem
import com.biat.ui.primitives.popover.Popover
import com.biat.ui.primitives.select.Select
import com.biat.ui.primitives.sheet.Sheet
import com.biat.ui.primitives.tabs.TabValue
import com.biat.ui.primitives.tabs.Tabs
import com.biat.ui.primitives.tooltip.Tooltip

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SampleApp() }
    }
}

/**
 * Demo styling lives HERE (sample app), never in the library.
 * Every Box/background below is user-owned visuals proving the
 * primitives are unstyled infrastructure.
 */
@Composable
fun SampleApp() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        DialogDemo()
        ControlledDialogDemo()
        PopoverDemo()
        MenuDemo()
        TooltipDemo()
        SelectDemo()
        TabsDemo()
        SheetDemo()
    }
}

@Composable
private fun DemoCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F1F1))
            .padding(16.dp),
    ) {
        BasicText(title)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun DemoButton(label: String) {
    Box(
        modifier = Modifier
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        BasicText(label, style = TextStyle(color = Color.White))
    }
}

@Composable
private fun DialogDemo() {
    val state = rememberDialogState()
    DemoCard("Dialog") {
        Dialog(
            state = state,
            trigger = { DemoButton(if (state.isOpen) "Close dialog" else "Open dialog") },
            scrim = { Box(Modifier.fillMaxSize().background(Color(0x88000000))) },
        ) {
            Box(Modifier.background(Color.White).padding(24.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Headless dialog content")
                    DialogClose(state = state) {
                        DemoButton("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlledDialogDemo() {
    var open by remember { mutableStateOf(false) }
    val state = rememberDialogState(
        controlledOpen = open,
        onOpenChange = { open = it },
    )
    DemoCard("Controlled dialog (open: $open)") {
        Dialog(
            state = state,
            trigger = { DemoButton("Toggle controlled") },
            scrim = { Box(Modifier.fillMaxSize().background(Color(0x88000000))) },
        ) {
            Box(Modifier.background(Color.White).padding(24.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Caller owns the open boolean")
                    DialogClose(state = state) {
                        DemoButton("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun PopoverDemo() {
    val state = rememberPopoverState()
    DemoCard("Popover") {
        Popover(
            state = state,
            trigger = { DemoButton("Toggle popover") },
        ) {
            Box(Modifier.background(Color.White).padding(16.dp)) {
                BasicText("Popover body (user-styled)")
            }
        }
    }
}

@Composable
private fun MenuDemo() {
    val state = rememberMenuState(itemCount = 3)
    var chosen by remember { mutableStateOf("none") }
    DemoCard("Menu (chosen: $chosen)") {
        Menu(
            state = state,
            trigger = { DemoButton("Open menu") },
        ) {
            listOf("Edit", "Duplicate", "Delete").forEach { item ->
                MenuItem(state = state, label = item, onSelect = { chosen = item }) {
                    Box(Modifier.fillMaxWidth().background(Color.White).padding(12.dp)) {
                        BasicText(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun TooltipDemo() {
    val state = rememberTooltipState()
    DemoCard("Tooltip") {
        Tooltip(
            state = state,
            tip = "Save document",
            overlay = {
                Box(Modifier.background(Color.Black).padding(8.dp)) {
                    BasicText(
                        "Save document",
                        style = TextStyle(color = Color.White),
                    )
                }
            },
            anchor = { DemoButton("Hover / focus me") },
        )
    }
}

@Composable
private fun SelectDemo() {
    val state = rememberSelectState(initialSelected = "Kotlin")
    val options = listOf("Kotlin", "Java", "Rust")
    DemoCard("Select (selected: ${state.selected})") {
        Select(
            state = state,
            options = options,
            trigger = { selected -> DemoButton(selected ?: "Pick language") },
            option = { value, highlighted, selected ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(if (highlighted) Color.LightGray else Color.White)
                        .padding(12.dp),
                ) {
                    BasicText((if (selected) "* " else "") + value)
                }
            },
        )
    }
}

@Composable
private fun TabsDemo() {
    val state = rememberTabsState(initialSelected = "a")
    val tabs = listOf(TabValue("a", "Account"), TabValue("b", "Password"))
    DemoCard("Tabs") {
        Tabs(
            state = state,
            tabs = tabs,
            tab = { item, selected, _ ->
                Box(
                    Modifier
                        .background(if (selected) Color.Black else Color.Gray)
                        .padding(12.dp),
                ) {
                    BasicText(
                        item.label,
                        style = TextStyle(color = Color.White),
                    )
                }
            },
            panel = { selected ->
                Box(Modifier.padding(top = 12.dp)) {
                    BasicText("Panel: ${selected?.label ?: "none"}")
                }
            },
        )
    }
}

@Composable
private fun SheetDemo() {
    val state = rememberSheetState()
    DemoCard("Sheet") {
        Sheet(
            state = state,
            trigger = { DemoButton("Open sheet") },
            scrim = { Box(Modifier.fillMaxSize().background(Color(0x88000000))) },
        ) {
            Box(Modifier.fillMaxWidth().background(Color.White).padding(24.dp)) {
                BasicText("Headless sheet content")
            }
        }
    }
}
