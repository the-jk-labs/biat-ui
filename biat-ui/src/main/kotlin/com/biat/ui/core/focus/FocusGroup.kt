package com.biat.ui.core.focus

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput

/** Registered focus targets shared by slot-based groups, including nested menus. */
internal class FocusGroup {
    val entries = mutableStateListOf<FocusEntry>()
    var current by mutableStateOf<FocusEntry?>(null)
    var pending by mutableStateOf<FocusEntry?>(null)
    var onMove: (Int) -> Unit = {}
    var onFocused: (Int) -> Unit = {}
    var loop = true
    var initialIndex = 0

    fun enabledEntries() = entries.filter { it.enabled }

    fun tabStop(): FocusEntry? =
        current?.takeIf { it in entries && it.enabled } ?: entries.getOrNull(initialIndex)?.takeIf { it.enabled }
            ?: enabledEntries().firstOrNull()

    fun move(delta: Int) {
        val enabled = enabledEntries()
        if (enabled.isEmpty()) return
        val index = enabled.indexOf(current)
        request(
            enabled[
                if (index < 0) {
                    if (delta > 0) 0 else enabled.lastIndex
                } else if (loop) {
                    (index + delta).mod(enabled.size)
                } else {
                    (index + delta).coerceIn(0, enabled.lastIndex)
                },
            ],
        )
    }

    fun request(entry: FocusEntry?) {
        if (entry == null) return
        current = entry
        pending = entry
        onMove(entries.indexOf(entry))
        entry.onNavigate?.invoke()
    }
}

internal class FocusEntry(
    val requester: FocusRequester,
) {
    var enabled by mutableStateOf(true)
    var onNavigate: (() -> Unit)? = null
}

internal val LocalFocusGroup = staticCompositionLocalOf<FocusGroup?> { null }

/** Requests after recomposition, once the new tab stop can accept focus. */
@Composable
internal fun FocusGroupEffect(
    group: FocusGroup,
    initialFocus: Boolean = false,
) {
    var entered by remember(group) { mutableStateOf(false) }
    LaunchedEffect(group.pending, group.entries.size) {
        val target = group.pending ?: if (initialFocus && !entered) group.tabStop() else null
        if (target != null) {
            entered = true
            target.requester.safeRequestFocus()
            group.pending = null
        }
    }
}

/** A single keyboard tab stop; pointer and accessibility activation remain available on all items. */
@Composable
internal fun focusGroupItem(
    enabled: Boolean = true,
    preferred: Boolean = false,
    onNavigate: (() -> Unit)? = null,
): Modifier {
    val group = LocalFocusGroup.current ?: return Modifier
    val entry = remember(group) { FocusEntry(FocusRequester()) }
    entry.enabled = enabled
    entry.onNavigate = onNavigate
    val preference by rememberUpdatedState(preferred)
    DisposableEffect(group, entry) {
        group.entries.add(entry)
        if (preference && group.current == null) group.current = entry
        onDispose {
            group.entries.remove(entry)
            if (group.current === entry) group.current = null
        }
    }
    LaunchedEffect(preferred) {
        if (preferred) group.current = entry
    }
    return Modifier
        .focusRequester(entry.requester)
        .focusProperties { canFocus = enabled && group.tabStop() === entry }
        .pointerInput(group, entry, enabled) {
            if (enabled) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    group.current = entry
                    group.onFocused(group.entries.indexOf(entry))
                    waitForUpOrCancellation()
                }
            }
        }.onFocusChanged {
            if (it.hasFocus) {
                group.current = entry
                group.onFocused(group.entries.indexOf(entry))
            }
        }
}

/** Arrow/Home/End navigation without imposing a layout on the group. */
internal fun Modifier.focusGroupKeys(
    group: FocusGroup,
    horizontal: Boolean? = null,
    rtl: Boolean = false,
): Modifier =
    onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown || group.enabledEntries().isEmpty()) return@onPreviewKeyEvent false
        when (event.key) {
            Key.MoveHome -> group.request(group.enabledEntries().firstOrNull())
            Key.MoveEnd -> group.request(group.enabledEntries().lastOrNull())
            Key.DirectionDown -> if (horizontal != true) group.move(1) else return@onPreviewKeyEvent false
            Key.DirectionUp -> if (horizontal != true) group.move(-1) else return@onPreviewKeyEvent false
            Key.DirectionRight -> if (horizontal != false) group.move(if (rtl) -1 else 1) else return@onPreviewKeyEvent false
            Key.DirectionLeft -> if (horizontal != false) group.move(if (rtl) 1 else -1) else return@onPreviewKeyEvent false
            else -> return@onPreviewKeyEvent false
        }
        true
    }
