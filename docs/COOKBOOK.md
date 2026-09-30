# Headless Compose cookbook

These recipes target rc2 and use Foundation visuals. Place them in your
application module and replace the text, backgrounds and sizes with your own
design. The shared imports below and all recipe functions compile together.

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.biat.ui.core.state.SheetDetent
import com.biat.ui.core.state.rememberDialogState
import com.biat.ui.core.state.rememberRadioGroupState
import com.biat.ui.core.state.rememberSheetState
import com.biat.ui.core.state.rememberSliderState
import com.biat.ui.core.state.rememberTabsState
import com.biat.ui.primitives.dialog.Dialog
import com.biat.ui.primitives.dialog.DialogClose
import com.biat.ui.primitives.radiogroup.RadioGroup
import com.biat.ui.primitives.radiogroup.RadioGroupValue
import com.biat.ui.primitives.sheet.Sheet
import com.biat.ui.primitives.sheet.sheetDrag
import com.biat.ui.primitives.slider.Slider
import com.biat.ui.primitives.tabs.TabValue
import com.biat.ui.primitives.tabs.Tabs
import com.biat.ui.primitives.tabs.TabsActivation
import com.biat.ui.primitives.tabs.TabsOrientation
```

## Controlled dialog

Reflect `onOpenChange` into the owner value. DialogClose requests a close
through the same state. The primitive owns trigger interaction; its text is
the caller's visual.

```kotlin
@Composable
fun ControlledDialogRecipe() {
    var open by remember { mutableStateOf(false) }
    val state = rememberDialogState(controlledOpen = open, onOpenChange = { open = it })
    Dialog(
        state = state,
        label = "Edit profile",
        trigger = { BasicText("Edit profile") },
        scrim = { Box(Modifier.fillMaxSize().background(Color(0x66000000))) },
    ) {
        Column(Modifier.background(Color.White).padding(24.dp)) {
            BasicText("Profile settings")
            DialogClose(state) { BasicText("Done") }
        }
    }
}
```

For an alert, add `isAlert = true`; outside-click dismissal then defaults off.
Pass `initialFocusRequester` when a specific content control should receive
entry focus. Attach it to that control with `Modifier.focusRequester`.

## Horizontal radio choices

The Row is caller-owned. Arrow keys skip disabled values and select the next
enabled choice. Keep each value unique.

```kotlin
@Composable
fun RadioChoicesRecipe() {
    val state = rememberRadioGroupState(initialSelected = "daily")
    RadioGroup(
        state = state,
        options = listOf(
            RadioGroupValue("daily", "Daily"),
            RadioGroupValue("weekly", "Weekly"),
        ),
        layout = { items -> Row { items() } },
        label = "Digest frequency",
    ) { option, selected ->
        BasicText(if (selected) "[${option.label}]" else option.label)
    }
}
```

For heterogeneous layouts, use RadioGroupContent and call `scope.Item(...)`
inside your own layout. The shared focus group still registers each item.

## Vertical tabs with manual activation

The outer Row places the tab list beside its panel. Up/Down moves focus;
Enter/Space commits selection. Automatic activation selects on arrow movement.

```kotlin
@Composable
fun ManualTabsRecipe() {
    val state = rememberTabsState(initialSelected = "account")
    Row {
        Tabs(
            state = state,
            tabs = listOf(TabValue("account", "Account"), TabValue("privacy", "Privacy")),
            layout = { tabs -> Column { tabs() } },
            orientation = TabsOrientation.Vertical,
            activation = TabsActivation.Manual,
            label = "Settings sections",
            tab = { tab, selected, _ ->
                BasicText(if (selected) "[${tab.label}]" else tab.label)
            },
            panel = { selected -> BasicText(selected?.label ?: "Choose a section") },
        )
    }
}
```

## Slider with a custom range

`step` is a value increment, rather than a count of interior stops. The
library overlays track and thumb slots and measures their container width.
Position the thumb yourself. This recipe reserves a fixed 200dp track width;
the 12dp thumb travels across 188dp to fit inside it. Responsive designs
should derive the thumb position from measured constraints.

```kotlin
@Composable
fun SliderRecipe() {
    val state = rememberSliderState(initialValue = 40f, valueRange = 10f..70f, step = 5f)
    Slider(
        state = state,
        label = "Volume",
        valueText = "${state.value.toInt()} percent",
        track = { fraction ->
            Box(Modifier.width(200.dp).height(24.dp).background(Color.LightGray)) {
                Box(Modifier.fillMaxWidth(fraction).height(24.dp).background(Color.DarkGray))
            }
        },
        thumb = {
            Box(Modifier.offset(x = 188.dp * state.fraction).size(12.dp).background(Color.Black))
        },
    )
}
```

In controlled mode, pass `controlledValue` and `onValueChange`. The owner must
accept and reflect the snapped value. Keep supplied values valid for the
range/step; the slider does not normalize the owner's controlled value.

## Sheet with caller-defined detent heights

The layout places a narrow sheet at the bottom. Dragging the whole content
measures thresholds against its height. If you attach sheetDrag to a smaller
handle, provide the full content height through `sheetHeightPx`.

```kotlin
@Composable
fun SheetRecipe() {
    val state = rememberSheetState()
    val height = when (state.detent) {
        SheetDetent.Peek -> 160.dp
        SheetDetent.Half -> 320.dp
        SheetDetent.Full -> 520.dp
    }
    Sheet(
        state = state,
        label = "Details",
        trigger = { BasicText("Show details") },
        layout = { content ->
            Box(Modifier.align(Alignment.BottomCenter).width(280.dp)) { content() }
        },
        scrim = { Box(Modifier.fillMaxSize().background(Color(0x66000000))) },
    ) {
        Column(
            Modifier.fillMaxWidth().height(height).background(Color.White).sheetDrag(state),
        ) {
            BasicText("Drag to change detent or dismiss", Modifier.padding(16.dp))
        }
    }
}
```

The trigger supplies the focus-return anchor. When opening a Sheet without a
trigger, the application must restore focus to its own control after closing.
Detents describe state; the application maps them to sizes and animations.

## More working examples

The [sample app](../sample/src/main/kotlin/com/biat/sample/MainActivity.kt) includes
nested dialogs, menus with checkbox/radio/submenu items, a text-field combobox
with async options, popup positioning, tooltip long-press, toggle groups and
toolbars. For adoption changes see [MIGRATION.md](MIGRATION.md); for automated
and manual validation see [CERTIFICATION.md](CERTIFICATION.md).
