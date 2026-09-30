package com.biat.ui.primitives.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import com.biat.ui.core.accessibility.dialogSemantics
import com.biat.ui.core.dismiss.consumeOverlayTaps
import com.biat.ui.core.dismiss.onEscape
import com.biat.ui.core.dismiss.outsideClick
import com.biat.ui.core.focus.FocusReturnEffect
import com.biat.ui.core.focus.FocusTrapEffect
import com.biat.ui.core.focus.focusTrap
import com.biat.ui.core.focus.rememberFocusReturnRequester
import com.biat.ui.core.focus.rememberFocusTrapRequester
import com.biat.ui.core.focus.rememberFocusTrapState
import com.biat.ui.core.portal.BiatPortal
import com.biat.ui.core.state.SheetState
import com.biat.ui.core.state.rememberSheetState

/**
 * Headless Sheet overlay behavior. Zero styling or animation:
 * [layout] owns content sizing and positioning. Provides portal, focus trap,
 * ESC + outside-click dismiss, detents, and drag-to-dismiss.
 *
 * - [trigger] (optional) renders inline and opens the sheet on click.
 *   When provided, focus returns to it whenever the sheet closes.
 *   Without a trigger the caller opens via state and owns focus; no
 *   anchor is known to return to.
 * - [content] height sizes the outside-click dismiss boundary: keep it
 *   wrap-content. Full-height content disables scrim dismissal.
 * - [label] is exposed as the sheet content description for screen readers.
 * - Detents ([SheetDetent] Peek/Half/Full) live in [SheetState.detent];
 *   render content per detent and attach [sheetDrag] to the caller's drag
 *   handle for finger-following drags with settle-on-release.
 */
@Composable
fun Sheet(
    state: SheetState = rememberSheetState(),
    dismissOnOutsideClick: Boolean = true,
    dismissOnEscape: Boolean = true,
    dismissOnBackPress: Boolean = true,
    label: String? = null,
    layout: @Composable BoxScope.(content: @Composable () -> Unit) -> Unit,
    trigger: (@Composable () -> Unit)? = null,
    scrim: @Composable (BoxScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    if (trigger != null) {
        val source = remember { MutableInteractionSource() }
        val returnRequester = rememberFocusReturnRequester()
        FocusReturnEffect(isOpen = state.isOpen, returnRequester = returnRequester)
        Box(
            modifier =
                Modifier
                    .focusRequester(returnRequester)
                    .focusProperties { canFocus = true }
                    .clickable(
                        interactionSource = source,
                        indication = null,
                        onClick = { state.open() },
                    ),
        ) {
            trigger()
        }
    }
    if (!state.isOpen) return

    val trapRequester = rememberFocusTrapRequester()
    val trapState = rememberFocusTrapState()
    val focusManager = LocalFocusManager.current
    FocusTrapEffect(active = true, trapRequester = trapRequester)

    // Dismiss boundary follows the content bounds: taps inside the sheet
    // are ignored even when they reach the scrim area. The scrim box shares
    // the outer box origin, so boundsInParent compares directly.
    var contentBounds by remember { mutableStateOf<Rect?>(null) }

    BiatPortal(
        onDismissRequest = { state.close() },
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = false,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .let { m ->
                            if (dismissOnOutsideClick) {
                                m.outsideClick(
                                    onOutsideClick = { state.close() },
                                    isInsideContent = { offset ->
                                        contentBounds?.contains(offset) == true
                                    },
                                )
                            } else {
                                m
                            }
                        },
            ) {
                scrim?.invoke(this)
            }
            layout {
                Box(
                    modifier =
                        Modifier
                            .onGloballyPositioned { contentBounds = it.boundsInRoot() }
                            .consumeOverlayTaps()
                            .dialogSemantics(label)
                            .focusTrap(
                                active = true,
                                trapRequester = trapRequester,
                                focusManager = focusManager,
                                trapState = trapState,
                                onEscape = if (dismissOnEscape) ({ state.close() }) else null,
                            ).let { m ->
                                if (dismissOnEscape) m.onEscape { state.close() } else m
                            },
                ) {
                    content()
                }
            }
        }
    }
}
