# Behavior certification

Automated semantics tests inspect the properties and actions exposed to
accessibility services. They do not certify spoken TalkBack output or a
human keyboard interaction on every device.

| Target | Coverage | Status |
|---|---|---|
| HONOR ALT-LX1, Android 14 / API 34 | Full instrumented suite, including focus, keys, dismiss, semantics and accessibility actions | 86 tests passed on 2026-09-30 at commit 1c6c84f |
| Google APIs x86_64 emulator, API 24 | Full instrumented suite at the library minimum SDK | Required CI job; see the run for each commit |
| Google APIs x86_64 emulator, API 36 | Full instrumented suite on a recent Android API | Required CI job; see the run for each commit |
| Manual TalkBack, API 34 | Spoken labels, values, state, traversal and activation | Pending; automated semantics checks are not manual certification |
| Physical keyboard, API 34 | Tab/Shift+Tab, arrows, Enter/Space, Home/End, Escape and trigger focus return | Automated key injection passed; human pass pending |

CI keeps per-API reports and emulator startup logs as workflow artifacts.
Publishing waits for both emulator jobs and the build/lint/API job.

## Local verification

With a connected Android device:

```bash
./gradlew :biat-ui:connectedDebugAndroidTest
```

On a Linux runner with Android SDK command-line tools and KVM:

```bash
bash scripts/run-instrumented-tests.sh 24
bash scripts/run-instrumented-tests.sh 36
```

## Manual pass

Enable TalkBack and check each sample primitive with touch exploration and
swipe navigation. Confirm labels, selected/checked/expanded state, slider
adjustment, disabled behavior, overlay entry/exit, and exclusion of decorative
separators. Record the device, Android API, TalkBack version, commit and result.
Repeat with a physical keyboard; check the entire open/navigate/activate/close
sequence and focus return, including nested overlays and disabled entries.
