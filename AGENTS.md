# AGENTS.md: biat-ui contributor guide (humans + AI agents)

This repo is a **headless UI primitives library** for Jetpack Compose
(Radix/Bits-style). Behavior is the product. There is no design system here.

## Golden rules (never violate)

1. **No visuals in `biat-ui/`.** No Material (`material`, `material3`), no colors,
   typography, shapes, spacing, elevation, or opinionated layouts in the library.
   Styling demos belong in `sample/` only.
2. **Three layers per primitive:**
   - `core/state/`: logic only (`mutableStateOf`, open/close/select transitions).
   - `core/` behavior (`focus/`, `dismiss/`, `portal/`, `accessibility/`): library-owned.
   - `primitives/*/`: thin composable structure wiring state + behavior to
     user-provided `trigger` / `content` slots. No default visuals.
3. **Composability over convenience.** Expose `Trigger`/`Content`/`Close` slots.
   Never hardcode a Button, Card, Surface, scrim color, padding, or animation.
4. **Accessibility is mandatory.** Every primitive wires semantic roles, screen-reader
   labels, focus order, and keyboard handling (Esc + arrows + Tab/Home/End where relevant).
5. **No new third-party dependencies** without discussion. Allowed: `compose.ui`,
   `compose.foundation`, `compose.runtime`, `core-ktx`. Banned: material libraries.

## Repo layout

```
settings.gradle.kts          # includes :biat-ui, :sample
gradle/libs.versions.toml    # single source of truth for versions
biat-ui/                     # the published library (namespace com.biat.ui)
  src/main/kotlin/com/biat/ui/
    core/state/          # DialogState, PopoverState, MenuState, ...
    core/focus/          # FocusTrap, RovingFocus
    core/dismiss/        # onEscape, outsideClick
    core/portal/         # BiatPortal (androidx.compose.ui.window.Dialog)
    core/accessibility/  # semantics helpers
    primitives/dialog|popover|menu|tooltip|select|tabs|sheet/
      accordion|collapsible|toggle|togglegroup|radiogroup|slider|
      toolbar|separator|label|avatar/
  src/test/              # pure state-machine unit tests (JUnit4)
sample/                      # demo app; ONLY place allowed to style things
AGENTS.md | ROADMAP.md | README.md
```

## How to add / change a primitive

1. Add `core/state/<Name>State.kt` + `remember<Name>State()` first. Keep it
   UI-free and unit-testable (see `biat-ui/src/test/.../StateMachinesTest.kt`).
2. Reuse behavior from `core/`: `BiatPortal` for overlays, `focusTrap` for modals,
   `rovingKeys`/`RovingFocusState` for lists, `onEscape`/`outsideClick` for dismiss,
   `core/accessibility/*` for semantics.
3. Add `primitives/<name>/<Name>.kt` with slot API:
   `trigger: @Composable () -> Unit`, `content: @Composable () -> Unit`
   (or typed variants). Never add default styling params.
4. Extend `StateMachinesTest` for new state transitions.
5. Add a demo section in `sample/.../MainActivity.kt` proving arbitrary styling works.

## Build / test

```bash
./gradlew :biat-ui:testDebugUnitTest      # JVM state-machine tests
./gradlew :biat-ui:assembleRelease        # library AAR
./gradlew :sample:assembleDebug           # demo APK
./gradlew ktlintCheck                     # style gate (ktlint-cli, .editorconfig)
./gradlew :biat-ui:apiCheck               # public API vs biat-ui/api/current.txt
./gradlew :biat-ui:dokkaHtml              # KDoc HTML into biat-ui/build/dokka
./gradlew :biat-ui:publishToMavenLocal    # publish dry-run into ~/.m2
```

Requirements: JDK 17, Android SDK (`sdk.dir` in `local.properties` or
`ANDROID_HOME`/`ANDROID_SDK_ROOT`). Compile SDK 37, min SDK 24.
Toolchain: Kotlin 2.4.20, AGP 9.4.1 (built-in Kotlin; do NOT apply
`org.jetbrains.kotlin.android`, AGP 9 fails the build if you try),
Gradle 9.8.0, Compose BOM 2026.09.00.

Gradle plugins that hook the Kotlin Gradle plugin source sets do not work
here (binary-compatibility-validator, Dokka Gradle plugin, ktlint Gradle
plugin register zero tasks). Wire such tooling as CLI tasks instead, as
done for `ktlintCheck`/`ktlintFormat` (ktlint-cli), `:biat-ui:dokkaHtml`
(dokka-cli + generated JSON config), and `:biat-ui:apiDump`/`:biat-ui:apiCheck`
(javap signature snapshot in `biat-ui/api/current.txt`, enforced by `check`).

## Code style

- Kotlin official style, `jvmTarget = 17`.
- No emojis or em-dashes in code, docs, or comments.
- Public API needs KDoc explaining the behavior contract.
- Prefer `Modifier` extension behavior helpers over wrapper Box soup.
- Keep files small and single-responsibility; one concept per file.

## Definition of done for a primitive

- [ ] State machine unit-tested (open/close/select/keyboard transitions)
- [ ] ESC + outside-click/back-press dismiss wired
- [ ] Focus correct (trap for modal, roving for lists, return focus on close)
- [ ] Semantics roles + labels present
- [ ] Zero styling in library; sample demo shows custom visuals
- [ ] `:biat-ui:apiDump` refreshed if public signatures changed (`apiCheck` enforces)
- [ ] AGENTS.md / ROADMAP.md updated if scope changed
