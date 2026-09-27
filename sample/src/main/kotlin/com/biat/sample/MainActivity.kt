package com.biat.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.biat.ui.core.positioning.PopupAlign
import com.biat.ui.core.positioning.PopupSide
import com.biat.ui.core.state.SheetDetent
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
import com.biat.ui.primitives.menu.MenuCheckboxItem
import com.biat.ui.primitives.menu.MenuItem
import com.biat.ui.primitives.menu.MenuRadioItem
import com.biat.ui.primitives.menu.MenuSeparator
import com.biat.ui.primitives.menu.MenuSub
import com.biat.ui.primitives.popover.Popover
import com.biat.ui.primitives.select.Select
import com.biat.ui.primitives.sheet.Sheet
import com.biat.ui.primitives.sheet.sheetDrag
import com.biat.ui.primitives.tabs.TabValue
import com.biat.ui.primitives.tabs.Tabs
import com.biat.ui.primitives.tabs.TabsActivation
import com.biat.ui.primitives.tabs.TabsOrientation
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
        DialogDepthDemo()
        PopoverDemo()
        PopoverPlacementDemo()
        PopoverDepthDemo()
        MenuDemo()
        MenuDepthDemo()
        TooltipDemo()
        TooltipDepthDemo()
        SelectDemo()
        ComboboxDemo()
        TabsDemo()
        TabsDepthDemo()
        SheetDemo()
        SheetDepthDemo()
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
private fun DialogDepthDemo() {
    val outer = rememberDialogState()
    val inner = rememberDialogState()
    val alert = rememberDialogState()
    val focused = rememberDialogState()
    val confirmFocus = remember { FocusRequester() }
    DemoCard("Dialog depth (nested, alert, initial focus)") {
        Dialog(
            state = outer,
            trigger = { DemoButton("Open outer") },
            scrim = { Box(Modifier.fillMaxSize().background(Color(0x88000000))) },
        ) {
            Box(Modifier.background(Color.White).padding(24.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Outer dialog")
                    Dialog(
                        state = inner,
                        trigger = { DemoButton("Open inner") },
                        scrim = { Box(Modifier.fillMaxSize().background(Color(0x66000000))) },
                    ) {
                        Box(Modifier.background(Color.White).padding(24.dp)) {
                            BasicText("Inner dialog")
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Dialog(
            state = alert,
            isAlert = true,
            trigger = { DemoButton("Open alert") },
            scrim = { Box(Modifier.fillMaxSize().background(Color(0x88000000))) },
        ) {
            Box(Modifier.background(Color.White).padding(24.dp)) {
                BasicText("Alert: scrim tap will not close me")
            }
        }
        Spacer(Modifier.height(8.dp))
        Dialog(
            state = focused,
            initialFocusRequester = confirmFocus,
            trigger = { DemoButton("Open focused") },
            scrim = { Box(Modifier.fillMaxSize().background(Color(0x88000000))) },
        ) {
            Box(Modifier.background(Color.White).padding(24.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Confirm lands focused")
                    Box(Modifier.focusRequester(confirmFocus).focusable()) {
                        DemoButton("Confirm")
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
private fun PopoverPlacementDemo() {
    val state = rememberPopoverState()
    DemoCard("Popover placement (top-end, 8.dp gap)") {
        Popover(
            state = state,
            side = PopupSide.Top,
            align = PopupAlign.End,
            sideOffset = 8.dp,
            trigger = { DemoButton("Toggle placed popover") },
        ) {
            Box(Modifier.background(Color.White).padding(16.dp)) {
                BasicText("Placed above, aligned end")
            }
        }
    }
}

@Composable
private fun PopoverDepthDemo() {
    val state = rememberPopoverState()
    DemoCard("Popover depth (modal: Tab cycles inside, Esc closes)") {
        Popover(
            state = state,
            modal = true,
            trigger = { DemoButton("Toggle modal popover") },
        ) {
            Column {
                Box(Modifier.focusable().background(Color.White).padding(16.dp)) {
                    BasicText("Item one")
                }
                Box(Modifier.focusable().background(Color.White).padding(16.dp)) {
                    BasicText("Item two")
                }
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
private fun MenuDepthDemo() {
    // 6 highlightable entries: Edit, Delete(disabled), checkbox, 2 radios, submenu.
    val state = rememberMenuState(itemCount = 6, disabledIndices = setOf(1))
    val subState = rememberMenuState(itemCount = 2)
    var chosen by remember { mutableStateOf("none") }
    var showHidden by remember { mutableStateOf(false) }
    var sort by remember { mutableStateOf("Name") }
    DemoCard("Menu depth (chosen: $chosen, hidden: $showHidden, sort: $sort)") {
        Menu(
            state = state,
            trigger = { DemoButton("Open depth menu") },
        ) {
            MenuItem(state = state, label = "Edit", onSelect = { chosen = "Edit" }) {
                MenuRow("Edit")
            }
            MenuItem(
                state = state,
                label = "Delete",
                enabled = false,
                onSelect = { chosen = "Delete" },
            ) {
                MenuRow("Delete (disabled)")
            }
            MenuSeparator {
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color.Gray))
            }
            MenuCheckboxItem(
                checked = showHidden,
                onCheckedChange = { showHidden = it },
                label = "Show hidden",
            ) {
                MenuRow((if (showHidden) "[x] " else "[ ] ") + "Show hidden")
            }
            listOf("Name", "Date").forEach { value ->
                MenuRadioItem(
                    state = state,
                    selected = sort == value,
                    onSelect = { sort = value },
                    label = "Sort by $value",
                ) {
                    MenuRow((if (sort == value) "(o) " else "( ) ") + "Sort by $value")
                }
            }
            MenuSub(
                state = subState,
                label = "Share",
                trigger = { MenuRow("Share >") },
            ) {
                MenuItem(
                    state = subState,
                    label = "Copy link",
                    onSelect = { chosen = "Copy link"; subState.close(); state.close() },
                ) {
                    MenuRow("Copy link")
                }
                MenuItem(
                    state = subState,
                    label = "Email",
                    onSelect = { chosen = "Email"; subState.close(); state.close() },
                ) {
                    MenuRow("Email")
                }
            }
        }
    }
}

@Composable
private fun MenuRow(label: String) {
    Box(Modifier.fillMaxWidth().background(Color.White).padding(12.dp)) {
        BasicText(label)
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
private fun TooltipDepthDemo() {
    val state = rememberTooltipState()
    DemoCard("Tooltip depth (end side, long-press on touch)") {
        Tooltip(
            state = state,
            tip = "Delete item",
            side = PopupSide.End,
            overlay = {
                Box(Modifier.background(Color.Black).padding(8.dp)) {
                    BasicText(
                        "Delete item",
                        style = TextStyle(color = Color.White),
                    )
                }
            },
            anchor = { DemoButton("Hover, focus, or long-press me") },
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
private fun ComboboxDemo() {
    val state = rememberSelectState<String>()
    val scope = rememberCoroutineScope()
    var allOptions by remember { mutableStateOf(listOf("Kotlin", "Java", "Rust")) }
    var loading by remember { mutableStateOf(false) }
    DemoCard("Combobox (query: ${state.query}, selected: ${state.selected})") {
        Select(
            state = state,
            options = allOptions,
            isLoading = loading,
            loading = {
                Box(Modifier.fillMaxWidth().background(Color.White).padding(12.dp)) {
                    BasicText("Loading...")
                }
            },
            empty = {
                Box(Modifier.fillMaxWidth().background(Color.White).padding(12.dp)) {
                    BasicText("No matches")
                }
            },
            trigger = {
                Box(Modifier.fillMaxWidth().background(Color.White).padding(12.dp)) {
                    if (state.query.isEmpty() && state.selected != null) {
                        BasicText(state.selected!!)
                    } else {
                        BasicTextField(
                            value = state.query,
                            onValueChange = { state.setQuery(it) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            },
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
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .background(Color.Black)
                    .clickable {
                        scope.launch {
                            loading = true
                            delay(800)
                            allOptions = listOf("Kotlin", "Java", "Rust", "Go")
                            loading = false
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                BasicText("Reload async", style = TextStyle(color = Color.White))
            }
            if (state.selected != null) {
                Box(
                    Modifier
                        .background(Color.Black)
                        .clickable {
                            state.clearSelection()
                            state.clearQuery()
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    BasicText("Clear", style = TextStyle(color = Color.White))
                }
            }
        }
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
private fun TabsDepthDemo() {
    val manual = rememberTabsState(initialSelected = "a")
    val vertical = rememberTabsState(initialSelected = "b")
    val tabs = listOf(
        TabValue("a", "Account"),
        TabValue("b", "Password"),
        TabValue("c", "Billing"),
    )
    DemoCard("Tabs depth (manual + vertical)") {
        BasicText("Manual: arrows move focus, Enter selects")
        Tabs(
            state = manual,
            tabs = tabs,
            activation = TabsActivation.Manual,
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
                Box(Modifier.padding(top = 12.dp, bottom = 12.dp)) {
                    BasicText("Panel: ${selected?.label ?: "none"}")
                }
            },
        )
        BasicText("Vertical: Up/Down navigate")
        Tabs(
            state = vertical,
            tabs = tabs,
            orientation = TabsOrientation.Vertical,
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

@Composable
private fun SheetDepthDemo() {
    val state = rememberSheetState()
    DemoCard("Sheet depth (detent: ${state.detent}, drag the handle)") {
        Sheet(
            state = state,
            trigger = { DemoButton("Open depth sheet") },
            scrim = { Box(Modifier.fillMaxSize().background(Color(0x88000000))) },
        ) {
            val height = when (state.detent) {
                SheetDetent.Peek -> 160.dp
                SheetDetent.Half -> 320.dp
                SheetDetent.Full -> 520.dp
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(height)
                    .background(Color.White)
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .background(Color.LightGray)
                            .sheetDrag(state),
                    ) {
                        BasicText("Drag handle (${state.detent})")
                    }
                    BasicText("Fling or drag past a quarter to move stops")
                    BasicText("Drag past half, or fling from Peek, to dismiss")
                }
            }
        }
    }
}
