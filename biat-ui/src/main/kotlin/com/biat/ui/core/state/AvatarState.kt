package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/** Load phase of a headless Avatar image. */
enum class AvatarStatus { Loading, Loaded, Error }

/**
 * Logic-only state for a headless Avatar. No UI, no styling.
 *
 * The caller drives [markLoaded]/[markError] from its image loader; the
 * primitive renders [content] when [status] is Loaded and the caller fallback
 * otherwise. Every transition is mirrored to [onStatusChange].
 */
@Stable
class AvatarState(
    initialStatus: AvatarStatus = AvatarStatus.Loading,
    var onStatusChange: ((AvatarStatus) -> Unit)? = null,
) {
    var status: AvatarStatus by mutableStateOf(initialStatus)
        private set

    val showFallback: Boolean get() = status != AvatarStatus.Loaded

    fun markLoaded() = updateStatus(AvatarStatus.Loaded)

    fun markError() = updateStatus(AvatarStatus.Error)

    fun reset() = updateStatus(AvatarStatus.Loading)

    private fun updateStatus(next: AvatarStatus) {
        if (status == next) return
        status = next
        onStatusChange?.invoke(next)
    }
}

@Composable
fun rememberAvatarState(
    initialStatus: AvatarStatus = AvatarStatus.Loading,
    onStatusChange: ((AvatarStatus) -> Unit)? = null,
): AvatarState =
    remember {
        AvatarState(initialStatus = initialStatus)
    }.apply {
        this.onStatusChange = onStatusChange
    }
