package com.biat.ui.core.portal

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Headless portal: renders [content] in a platform dialog window so overlays
 * escape parent clipping / z-order. Deliberately unstyled: the user owns
 * all visuals inside [content].
 *
 * The scrim / positioning is the caller's responsibility.
 */
@Composable
fun BiatPortal(
    onDismissRequest: () -> Unit,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = false,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties =
            DialogProperties(
                dismissOnBackPress = dismissOnBackPress,
                dismissOnClickOutside = dismissOnClickOutside,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        content = content,
    )
}
