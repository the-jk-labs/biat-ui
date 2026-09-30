# Migrating to headless primitives

The current development version is `1.0.0-rc2`. Its required layout slots
change source and binary signatures from rc1. Recompile consumers and update
call sites before adopting it; the stable 1.0 API freeze is still pending.

## From rc1 to rc2

Supply `layout` for Menu, MenuSub, Select, Tabs, Toolbar, RadioGroup,
ToggleGroup, Accordion, AccordionItem, Collapsible and Sheet. The library
wraps slots only for behavior; your layout arranges their children.

For a list or horizontal group, add the appropriate caller layout:

```kotlin
layout = { items -> Column { items() } }
```

```kotlin
layout = { items -> Row { items() } }
```

Tabs' layout arranges the tab list only. The panel remains a sibling emitted
after the list: put Tabs inside your own Row or Column to arrange both.
Tabs and Toolbar `orientation` chooses keyboard keys; it does not choose a
visual layout. Match it to your arrangement.

Accordion's `layout` arranges items; `itemLayout` arranges each trigger and
panel. It defaults to your `layout`. If items run horizontally but panels
stack vertically, explicitly supply `itemLayout = { Column { it() } }`.

RadioGroupContent and ToggleGroupContent now take ordinary composable
callbacks with their group scope. Add your own Row/Column inside the callback;
RowScope/ColumnScope modifiers belong in that caller layout.

Sheet no longer assumes bottom alignment or full width. To restore a bottom
sheet, use its BoxScope layout slot:

```kotlin
layout = { sheetContent ->
    Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
        sheetContent()
    }
}
```

The measured content rectangle defines inside/outside dismissal. Put sizing
on the content you want to protect from outside taps; a full-screen content
rectangle leaves no outside region. The scrim remains caller-owned.

## From Material to headless

Keep Material in the application module if your application uses it. The
library supplies state and interaction, so move colors, typography, shapes,
spacing, elevation and animations into your slot content.

| Existing UI | Headless primitive | Application owns |
|---|---|---|
| Dialog / AlertDialog | Dialog (`isAlert` for explicit dismissal) | Card, actions, scrim and initial focus target |
| DropdownMenu | Menu + MenuItem | Menu layout and item visuals |
| Dropdown selection | Select | Trigger, option visuals, loading/empty content |
| TabRow / Tab | Tabs | List/panel arrangement and selected styling |
| Modal bottom sheet | Sheet + sheetDrag | Position, width, detent heights, scrim and drag region |
| Checkbox / Switch | Checkbox / Switch + ToggleState | Checked/disabled visuals and surrounding labels |
| Radio choices / segmented controls | RadioGroup / ToggleGroup | Arrangement and selected/pressed visuals |
| Slider | Slider + SliderState | Measured track and positioned thumb |

Render passive visuals inside a primitive's interactive item/trigger slot.
The primitive already provides the click action and focus target. Adding
another clickable Button inside that slot can create competing actions or
extra focus stops. Independent controls inside Dialog/Popover content are
appropriate and remain caller-owned.

## State and accessibility

`initial*` arguments seed a remembered instance; changing them does not reset
it. In controlled mode, callbacks request a change and the owner must feed
the accepted value back through `controlled*`. Keep the mode consistent for
the instance's lifetime. Select selection, Tabs selection, radio selection,
accordion expansion, toggle-group membership and Sheet detents are currently
uncontrolled even when an overlay's openness is controlled.

Give groups and overlays meaningful labels, and keep option keys stable and
unique. Disabled visuals should reflect both primitive and state flags. Menu
`itemCount` counts actionable entries, including disabled entries and submenu
triggers, and excludes separators; `disabledIndices` must match disabled
entries so navigation skips them.

Roving groups expose one keyboard Tab stop. Menu arrows move actual item
focus; radio arrows also select, while toggle-group arrows move focus without
pressing. Manual Tabs require Enter/Space or pointer activation. Sliders expose
range information and snapped accessibility adjustment actions. Decorative
separators are excluded from accessibility traversal.

Run consumer keyboard and accessibility checks after migration. Automated
semantics coverage and the outstanding human checks are recorded in
[CERTIFICATION.md](CERTIFICATION.md). See [COOKBOOK.md](COOKBOOK.md) for recipes
and the [sample app](../sample/src/main/kotlin/com/biat/sample/MainActivity.kt)
for styled examples and combobox filtering.
