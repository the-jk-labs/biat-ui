# biat-ui — headless UI primitives for Jetpack Compose

Behavior-first, Radix/Bits-style primitives for Android. Interaction logic,
accessibility, state, focus, and keyboard navigation — **zero styling**.
All visuals are yours.

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

## Why

Design systems dictate pixels. `biat-ui` dictates **behavior**: open/close state
machines, focus traps, roving focus, portals, ESC + outside-click dismiss, and
screen-reader semantics. You bring any visual design; the library stays invisible.

## Primitives (v0.1.0)

| Primitive | State | Slots |
|---|---|---|
| Dialog | `DialogState` / `rememberDialogState` | `trigger`, `content`, `DialogTrigger`, `DialogClose` |
| Popover | `PopoverState` | `trigger`, `content` |
| Menu | `MenuState` (+highlight) / `MenuItem` | `trigger`, `content` |
| Tooltip | `TooltipState` | `anchor`, `overlay` |
| Select | `SelectState<T>` | `trigger`, `option` |
| Tabs | `TabsState` / `TabValue` | `tab`, `panel` |
| Sheet | `SheetState` (open + expanded) | `scrim`, `content` |

Core systems live in `com.biat.ui.core`: `state/`, `focus/` (trap + roving),
`dismiss/` (ESC + outside-click), `portal/` (`BiatPortal`), `accessibility/`.

## Non-goals

No Material dependency, no themes/colors/typography, no opinionated layouts.
See `AGENTS.md` golden rules.

## Quick start

`settings.gradle.kts` already includes `:biat-ui`. In your module:

```kotlin
implementation(project(":biat-ui"))
```

Requirements: JDK 17, Android SDK, compileSdk 37, minSdk 24.
(Kotlin 2.4.20, AGP 9.4.1 with built-in Kotlin, Gradle 9.8.0.)

```bash
./gradlew :biat-ui:testDebugUnitTest   # state-machine tests
./gradlew :biat-ui:assembleRelease     # AAR
./gradlew :sample:assembleDebug        # demo app
```

See `sample/` for styled usage examples (styling lives only there).

## Docs

- `AGENTS.md` — contributor rules (read before any PR)
- `ROADMAP.md` — v0.1 → v1.0 plan
- `biat-ui/src/main/kotlin/com/biat/ui/` — KDoc on every public API

## License

MPL-2.0 — see `LICENSE`.
