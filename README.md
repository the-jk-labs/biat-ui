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
| Tooltip | `TooltipState` | `trigger`, `content` |
| Select | `SelectState<T>` | `trigger`, `item` |
| Tabs | `TabsState` / `TabValue` | `tab`, `panel` |
| Sheet | `SheetState` | `scrim`, `content` |
| Collapsible | `CollapsibleState` | `trigger`, `content` |
| Accordion | `AccordionState` / `AccordionValue` | `trigger`, `content` |
| Checkbox / Switch / ToggleButton | `ToggleState` | `content` |
| ToggleGroup | `ToggleGroupState` / `ToggleGroupValue` | `item` |
| RadioGroup | `RadioGroupState` / `RadioGroupValue` | `item` |
| Slider | `SliderState` | `track`, `thumb` |
| Toolbar | `ToolbarState` / `ToolbarValue` | `item` |
| Separator | stateless | `content` |
| Label | stateless | `content` |
| Avatar | `AvatarState` | `fallback`, `content` |

Shared behavior lives in `com.biat.ui.core`: state machines, focus trap,
roving focus, portal, dismiss handling, and accessibility semantics.

The library has no Material dependency and defines no themes, colors, or
item arrangements. Multi-child primitives require a `layout` slot, such as
`layout = { items -> Row { items() } }`, supplied by the caller. Sheet's layout
slot has a `BoxScope` receiver for caller-owned sizing and positioning. All visuals belong to the caller; see `sample/` for examples.

## Build

Requires JDK 17 and the Android SDK (compile SDK 37, min SDK 24).

```bash
./gradlew :biat-ui:testDebugUnitTest   # unit tests
./gradlew :biat-ui:assembleRelease     # AAR
./gradlew :sample:assembleDebug        # demo app
```

## Versioning

`io.github.the-jk-labs:biat-ui` follows semantic versioning from 1.0.0. Behavior is the
product, so behavior-contract changes count as breaking too:

- Patch: bug fixes with no signature or behavior-contract change.
- Minor: new primitives, new optional parameters or slots, new state helpers.
- Major: renames, removals, new required parameters, behavior-contract
  changes (dismiss, focus, keyboard, semantics).

Signatures are pinned by `biat-ui/api/current.txt`; `:biat-ui:apiCheck`
fails CI on any drift. Accepting a change means refreshing the snapshot
with `:biat-ui:apiDump` and bumping the version to match.

## Publishing

Releases are fully automatic. Pushing a `v*` tag runs CI (lint, tests,
assembles, `apiCheck`) and then publishes `io.github.the-jk-labs:biat-ui`
to Maven Central with auto-release; artifacts appear within 10 to 30 minutes.
Dry-run locally without credentials via `./gradlew :biat-ui:publishToMavenLocal`.

Maintainers must provide four GitHub Actions secrets once:

| Secret | How to obtain |
|---|---|
| `CENTRAL_PORTAL_USERNAME` / `CENTRAL_PORTAL_PASSWORD` | Central Portal user token, not the login password (account menu, Generate User Token) |
| `SIGNING_KEY` | `gpg --export-secret-keys --armor <key id>` (create the key with a password) |
| `SIGNING_PASSWORD` | The key password |

The public key must be distributed to a keyserver (`gpg --send-keys`).

## Docs

- `AGENTS.md`: contributor rules
- `ROADMAP.md`: release plan
- `docs/CERTIFICATION.md`: automated coverage and manual certification status

## License

MPL-2.0. See `LICENSE`.
