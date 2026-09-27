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

- [x] True focus-trap cycling (Tab/Shift+Tab wrap via focusManager, automated
      on-device test: forward/backward wrap + containment + ESC)
- [x] Focus return on close for every overlay primitive (shared
      FocusReturnEffect; Tooltip exempt, focus never leaves its anchor)
- [x] Controlled vs uncontrolled state contract documented + tested
      (`initialOpen` vs `controlledOpen + onOpenChange`; notify-only in
      controlled mode, selection stays uncontrolled)
- [x] Outside-click vs inside-tap disambiguation tests (geometry exclusion +
      tap consumption, proven on-device for modifiers, Dialog, Sheet)
- [x] Popup positioning API for Popover/Menu/Select (side, align, Dp
      offsets, collision flip + window shift, pure resolver unit-tested)
- [x] Instrumented keyboard-nav tests (15 on-device tests: Esc closes
      Dialog/Sheet/Popover/Menu/Select, Enter/Down opens triggers, Menu/Select
      arrows + Home/End highlight, Select Enter commits, Tabs arrows wrap +
      Home/End, Tooltip focus shows overlay)
- [x] Screen-reader pass (TalkBack roles, labels, traversal order):
      8 on-device SemanticsTest tests pin Dialog/Sheet/Menu/TabList caller
      labels, MenuItem Button role + selection, Tab role + selection,
      Tooltip anchor content/state description, Select DropdownList role.
      New `label` params on Dialog/Sheet/Menu/Tabs close the gap where
      helpers accepted labels the primitives never exposed. Certified on
      ALT-LX1 (API 14): 32/32 instrumented, 24/24 JVM green.

## v0.3.0: Primitive depth

- [x] Select: typeahead filtering (Combobox), async options, clearable:
      query lives in SelectState (uncontrolled or controlled via
      controlledQuery + onQueryChange); setQuery opens + resets highlight,
      filteredOptions + moveHighlightToMatch are pure and unit-tested,
      close resets highlight like MenuState. Select renders the filtered
      list with loading/empty slots, clamps highlight as async options
      arrive, jumps highlight on single-key typeahead, and clears via
      Backspace/Delete on a closed trigger. Sample ComboboxDemo binds
      BasicTextField to query with simulated async reload. Certified on
      ALT-LX1 (API 14): 37/37 instrumented, 28/28 JVM green.
- [x] Menu: submenus, checkbox/radio items, separators, disabled items:
      disabled indices live in MenuState (moveHighlight skips, highlight
      snaps to enabled, all-disabled stays -1, unit-tested); MenuItem gains
      enabled (no click action, disabled semantics); MenuSeparator is a
      non-interactive slot excluded from itemCount; MenuCheckboxItem uses
      toggleable checkbox semantics and stays open; MenuRadioItem uses
      selectable radio semantics and closes; MenuSub anchors a nested menu
      to its trigger (click/ArrowRight opens, Esc/ArrowLeft closes only the
      submenu, focus returns to trigger, parent stays open). Sample
      MenuDepthDemo wires all five. Certified on ALT-LX1 (API 14):
      43/43 instrumented, 31/31 JVM green.
- [x] Dialog: nested dialogs, alert vs plain variants, initial-focus target:
      nesting layers naturally (own state + window per level, Esc/tap hits
      only the topmost, focus returns down the trigger chain, on-device
      certified); isAlert switches outside-click default off for
      explicit-action dialogs (explicit param still wins); new
      initialFocusRequester focuses a caller node at first layout with trap
      root fallback (composition-time requests race the portal window).
      Sample DialogDepthDemo wires all three. Certified on ALT-LX1 (API 14):
      47/47 instrumented, 31/31 JVM green.
- [x] Sheet: drag-to-dismiss + detents (peek/half/full), swipe velocity:
      detent lives in SheetState with onDetentChange (expand/collapse map to
      Full/Half, isExpanded derived); pure resolveSheetSettle maps
      drag + velocity to Dismiss/Snap (positional fractions, one-stop
      flings, configurable enabled stops, unit-tested); headless
      Modifier.sheetDrag attaches to caller handles with finger-following
      offset and settle-on-release. Fixed consumeOverlayTaps to contain
      taps on release instead of gobbling down/move, which had starved
      inner draggables. Sample SheetDepthDemo drives heights from detent.
      Certified on ALT-LX1 (API 14): 52/52 instrumented, 35/35 JVM green.
- [x] Tabs: automatic vs manual activation modes, vertical orientation, RTL arrows:
      TabsActivation Automatic (arrows select, the default) vs Manual
      (arrows move focus only, Enter/Space or click activates via the
      tab click handler); TabsOrientation Horizontal (Left/Right) vs
      Vertical (Up/Down, Left/Right ignored); focus follows arrows via
      per-tab requesters; pure resolveTabIndex (wrap, clamp) unit-tested.
      Sample TabsDepthDemo shows manual + vertical. Certified on ALT-LX1
      (API 14): 58/58 instrumented, 37/37 JVM green.
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
