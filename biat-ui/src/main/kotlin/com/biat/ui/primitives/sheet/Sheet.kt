package com.biat.ui.primitives.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import com.biat.ui.core.accessibility.dialogSemantics
import com.biat.ui.core.dismiss.consumeOverlayTaps
import com.biat.ui.core.dismiss.onEscape
import com.biat.ui.core.dismiss.outsideClick
import com.biat.ui.core.focus.FocusTrapEffect
import com.biat.ui.core.focus.focusTrap
import com.biat.ui.core.focus.rememberFocusTrapRequester
import com.biat.ui.core.portal.BiatPortal
import com.biat.ui.core.state.SheetState
import com.biat.ui.core.state.rememberSheetState

/**
 * Headless Sheet (bottom overlay behavior). Zero styling or animation:
 * caller owns visuals, drag gestures, and positioning. Provides portal,
 * focus trap, ESC + outside-click dismiss.
 */
@Composable
fun Sheet(
    state: SheetState = rememberSheetState(),
    dismissOnOutsideClick: Boolean = true,
    dismissOnEscape: Boolean = true,
    dismissOnBackPress: Boolean = true,
    scrim: @Composable (BoxScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    if (!state.isOpen) return

    val trapRequester = rememberFocusTrapRequester()
    val focusManager = LocalFocusManager.current
    FocusTrapEffect(active = true, trapRequester = trapRequester)

    BiatPortal(
        onDismissRequest = { state.close() },
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = false,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .let { m ->
                        if (dismissOnOutsideClick) m.outsideClick { state.close() } else m
                    },
            ) {
                scrim?.invoke(this)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .consumeOverlayTaps()
                    .dialogSemantics()
                    .focusTrap(
                        active = true,
                        trapRequester = trapRequester,
                        focusManager = focusManager,
                        onEscape = if (dismissOnEscape) ({ state.close() }) else null,
                    )
                    .let { m ->
                        if (dismissOnEscape) m.onEscape { state.close() } else m
                    },
                contentAlignment = Alignment.BottomCenter,
            ) {
                content()
            }
        }
    }
}
