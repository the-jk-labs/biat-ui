# ROADMAP.md: biat-ui

Headless UI primitives for Jetpack Compose. Behavior-first, zero styling.
Status as of v0.1.0 scaffold (2026-09-26).

## v0.1.0: Foundation (this init)

- [x] Gradle scaffold: `:biat-ui` library + `:sample` app, version catalog, wrapper
- [x] State layer: Dialog, Popover, Menu, Tooltip, Select, Tabs, Sheet
- [x] Behavior systems: focus trap, roving focus, portal, ESC + outside-click dismiss
- [x] Accessibility semantics helpers (dialog/menu/tab/tooltip roles)
- [x] Primitives: Dialog, Popover, Menu (+MenuItem), Tooltip, Select, Tabs, Sheet
- [x] State-machine unit tests (`StateMachinesTest`)
- [x] Sample app proving arbitrary user styling
- [x] AGENTS.md / ROADMAP.md / README.md

## v0.2.0: Behavior hardening

- [ ] True focus-trap cycling (Tab/Shift+Tab wrap via focusManager, automated test)
- [x] Focus return on close for every overlay primitive (shared
      FocusReturnEffect; Tooltip exempt, focus never leaves its anchor)
- [x] Controlled vs uncontrolled state contract documented + tested
      (`initialOpen` vs `controlledOpen + onOpenChange`; notify-only in
      controlled mode, selection stays uncontrolled)
- [ ] Outside-click vs inside-tap disambiguation tests (scrim consumption)
- [ ] Popup positioning API for Popover/Menu/Select (alignment, offset, collision flip)
- [ ] Instrumented keyboard-nav tests (Esc/arrows/Enter/Home/End per primitive)
- [ ] Screen-reader pass (TalkBack roles, labels, traversal order)

## v0.3.0: Primitive depth

- [ ] Select: typeahead filtering (Combobox), async options, clearable
- [ ] Menu: submenus, checkbox/radio items, separators, disabled items
- [ ] Dialog: nested dialogs, alert vs plain variants, initial-focus target
- [ ] Sheet: drag-to-dismiss + detents (peek/half/full), swipe velocity
- [ ] Tabs: automatic vs manual activation modes, vertical orientation, RTL arrows
- [ ] Tooltip: placement + collision avoidance, touch long-press trigger
- [ ] Popover: modal vs non-modal modes, anchor-follow on scroll/resize

## v0.4.0: New primitives (Bits parity)

- [ ] Accordion / Collapsible
- [ ] Checkbox / Switch / Radio-group (headless toggle behavior)
- [ ] Slider
- [ ] Toggle / Toggle-group
- [ ] Toolbar
- [ ] Avatar (fallback behavior) / Separator / Label primitives if justified
- [ ] Each ships with state + behavior + slots + tests + sample, per AGENTS.md

## v0.5.0: Polish for 1.0 candidacy

- [ ] Public API review: naming consistency (`Trigger/Content/Close/Item` suffixes)
- [ ] Binary-compatibility validation (binary-compatibility-validator plugin)
- [ ] Dokka API docs + behavior-contract docs per primitive
- [ ] Detekt/ktlint + API lint in CI
- [ ] Screenshot-free sample gallery (all styling in sample, themes switcher)

## v1.0.0: Stable

- [ ] Frozen public API, semantic versioning commitment
- [ ] Maven Central publish (`com.biat:biat-ui`), signing, SBOM
- [ ] Migration + cookbook docs (Material → headless, common recipes)
- [ ] TalkBack + keyboard certification matrix (devices/API levels)
- [ ] GitHub Actions CI: build, unit + instrumented tests, lint, publish

## Non-goals (never)

Material components, themes, colors, typography, opinionated layouts,
"beautiful defaults", design-system constraints. If a PR adds pixels to
`biat-ui/`, it gets rejected.
