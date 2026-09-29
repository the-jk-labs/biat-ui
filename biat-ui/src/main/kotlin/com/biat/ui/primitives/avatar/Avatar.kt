package com.biat.ui.primitives.avatar

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.biat.ui.core.accessibility.avatarSemantics
import com.biat.ui.core.state.AvatarState
import com.biat.ui.core.state.rememberAvatarState

/**
 * Headless Avatar with fallback behavior. Zero styling.
 *
 * Renders [content] (the loaded image) when [AvatarState.status] is Loaded;
 * otherwise renders [fallback] (initials, generic icon, caller-owned).
 * The caller drives [AvatarState.markLoaded]/[markError] from its image
 * loader; [label] names the avatar for screen readers.
 */
@Composable
fun Avatar(
    state: AvatarState = rememberAvatarState(),
    label: String? = null,
    fallback: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(modifier = Modifier.avatarSemantics(label)) {
        if (state.showFallback) {
            fallback()
        } else {
            content()
        }
    }
}
