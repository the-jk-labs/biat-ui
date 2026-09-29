# biat-ui

![CI](https://img.shields.io/github/actions/workflow/status/the-jk-labs/biat-ui/ci.yml?label=CI&color=black)
![License](https://img.shields.io/badge/license-MPL--2.0-black)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-black)
![Compose BOM](https://img.shields.io/badge/Compose_BOM-2026.09.00-black)

Headless UI primitives for Jetpack Compose. State, focus, keyboard, and
accessibility behavior without styling.

## Example

```kotlin
val state = rememberDialogState()

Dialog(
    state = state,
    trigger = { MyButton("Open") },
    scrim = { Box(Modifier.fillMaxSize().background(Color(0x88000000))) },
) {
    MyCard {
        BasicText("Content")
        DialogClose(state) { MyButton("Close") }
    }
}
```

## Primitives

| Primitive | State | Slots |
|---|---|---|
| Dialog | `DialogState` / `rememberDialogState` | `trigger`, `content`, `DialogTrigger`, `DialogClose` |
| Popover | `PopoverState` | `trigger`, `content` |
| Menu | `MenuState` / `MenuItem` | `trigger`, `content` |
| Tooltip | `TooltipState` | `anchor`, `overlay` |
| Select | `SelectState<T>` | `trigger`, `option` |
| Tabs | `TabsState` / `TabValue` | `tab`, `panel` |
| Sheet | `SheetState` | `scrim`, `content` |
| Collapsible | `CollapsibleState` | `trigger`, `content` |
| Accordion | `AccordionState` / `AccordionValue` | `trigger`, `content` |
| Checkbox / Switch / ToggleButton | `ToggleState` | `content` |
| ToggleGroup | `ToggleGroupState` / `ToggleGroupValue` | `item` |
| RadioGroup | `RadioGroupState` / `RadioValue` | `option` |
| Slider | `SliderState` | `track`, `thumb` |
| Toolbar | `ToolbarState` / `ToolbarValue` | `item` |
| Separator | stateless | `content` |
| Label | stateless | `content` |
| Avatar | `AvatarState` | `fallback`, `content` |

Shared behavior lives in `com.biat.ui.core`: state machines, focus trap,
roving focus, portal, dismiss handling, and accessibility semantics.

The library has no Material dependency and defines no themes, colors, or
layouts. All visuals belong to the caller; see `sample/` for examples.

## Build

Requires JDK 17 and the Android SDK (compile SDK 37, min SDK 24).

```bash
./gradlew :biat-ui:testDebugUnitTest   # unit tests
./gradlew :biat-ui:assembleRelease     # AAR
./gradlew :sample:assembleDebug        # demo app
```

## Docs

- `AGENTS.md`: contributor rules
- `ROADMAP.md`: release plan

## License

MPL-2.0. See `LICENSE`.
