package com.biat.ui.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Generic single-select state backing Select / Combobox.
 *
 * Openness contract (controlled vs uncontrolled):
 * - Uncontrolled (default): pass [initialOpen]; the state owns the value and
 *   [open]/[close]/[toggle]/[setOpen] mutate it, notifying [onOpenChange].
 * - Controlled: pass non-null [controlledOpen] plus [onOpenChange]; the caller
 *   owns the value and [open]/[close]/[toggle]/[setOpen] only notify via
 *   [onOpenChange] without mutating. The caller must reflect the new value
 *   back into [controlledOpen] ([rememberSelectState] does this every
 *   recomposition). Do not switch modes during the state's lifetime.
 * - Selection itself ([select]/[clearSelection]) stays uncontrolled; the
 *   committed value is mirrored to [onSelectedChange].
 *
 * Query contract (typeahead / Combobox filtering):
 * - Uncontrolled (default): pass [initialQuery]; [setQuery]/[clearQuery]
 *   mutate and notify [onQueryChange]. Typing via [setQuery] opens the
 *   listbox and resets highlight, matching Combobox behavior.
 * - Controlled: pass non-null [controlledQuery] plus [onQueryChange]; the
 *   caller owns the text and [setQuery] only notifies. Reflect back into
 *   [controlledQuery] ([rememberSelectState] does this every recomposition).
 * - Filtering itself is caller-visible via [filteredOptions]; the composable
 *   renders the filtered list. Async options work by passing a new [options]
 *   list whenever data arrives; highlight clamps to the visible range.
 */
@Stable
class SelectState<T>(
    initialOpen: Boolean = false,
    initialSelected: T? = null,
    initialQuery: String = "",
    controlledOpen: Boolean? = null,
    controlledQuery: String? = null,
    var onOpenChange: ((Boolean) -> Unit)? = null,
    val onSelectedChange: ((T?) -> Unit)? = null,
    var onQueryChange: ((String) -> Unit)? = null,
) {
    private var internalOpen by mutableStateOf(initialOpen)
    private var internalQuery by mutableStateOf(initialQuery)

    /**
     * Caller-owned value in controlled mode. Managed by [rememberSelectState];
     * assign only to reflect the caller's value.
     */
    var controlledOpen: Boolean? by mutableStateOf(controlledOpen)

    /** Caller-owned query in controlled mode. Managed by [rememberSelectState]. */
    var controlledQuery: String? by mutableStateOf(controlledQuery)

    val isOpen: Boolean get() = controlledOpen ?: internalOpen
    /** Current filter text. Empty means no filtering. */
    val query: String get() = controlledQuery ?: internalQuery
    var selected: T? by mutableStateOf(initialSelected)
        private set
    var highlightedIndex by mutableIntStateOf(-1)
        internal set

    fun open() = setOpen(true)
    fun close() {
        highlightedIndex = -1
        setOpen(false)
    }
    fun toggle() = setOpen(!isOpen)

    @JvmName("setOpenState")
    fun setOpen(open: Boolean) {
        if (isOpen == open) return
        if (controlledOpen != null) {
            onOpenChange?.invoke(open)
        } else {
            internalOpen = open
            onOpenChange?.invoke(open)
        }
    }

    fun select(value: T?) {
        selected = value
        onSelectedChange?.invoke(value)
        close()
    }

    fun clearSelection() = select(null)

    /**
     * Sets the filter text. Non-empty input opens the listbox and resets
     * highlight so navigation starts from the top of the filtered list.
     * In controlled-query mode this only notifies via [onQueryChange].
     */
    fun setQuery(value: String) {
        if (query == value) return
        if (controlledQuery != null) {
            onQueryChange?.invoke(value)
        } else {
            internalQuery = value
            onQueryChange?.invoke(value)
        }
        highlightedIndex = -1
        if (value.isNotEmpty() && !isOpen) open()
    }

    fun clearQuery() = setQuery("")

    /**
     * Returns [options] filtered by the current [query] using [toDisplay]
     * for text conversion. Empty query returns [options] unchanged.
     */
    fun filteredOptions(
        options: List<T>,
        toDisplay: (T) -> String = { it.toString() },
    ): List<T> {
        val q = query.trim()
        if (q.isEmpty()) return options
        return options.filter { toDisplay(it).contains(q, ignoreCase = true) }
    }

    /**
     * Moves highlight to the next visible option whose display text starts
     * with [input] (case-insensitive), wrapping from the current position.
     * Returns true when a match was found. Powers single-key typeahead when
     * no text field is bound to [query].
     */
    fun moveHighlightToMatch(
        options: List<T>,
        toDisplay: (T) -> String = { it.toString() },
        input: String,
    ): Boolean {
        if (options.isEmpty() || input.isEmpty()) return false
        val size = options.size
        val start = if (highlightedIndex < 0) 0 else {
            (highlightedIndex + 1) % size
        }
        for (step in 0 until size) {
            val index = (start + step) % size
            if (toDisplay(options[index]).startsWith(input, ignoreCase = true)) {
                highlightedIndex = index
                return true
            }
        }
        return false
    }
}

@Composable
fun <T> rememberSelectState(
    initialOpen: Boolean = false,
    initialSelected: T? = null,
    initialQuery: String = "",
    controlledOpen: Boolean? = null,
    controlledQuery: String? = null,
    onOpenChange: ((Boolean) -> Unit)? = null,
    onSelectedChange: ((T?) -> Unit)? = null,
    onQueryChange: ((String) -> Unit)? = null,
): SelectState<T> = remember {
    SelectState<T>(
        initialOpen = initialOpen,
        initialSelected = initialSelected,
        initialQuery = initialQuery,
        onSelectedChange = onSelectedChange,
    )
}.apply {
    this.controlledOpen = controlledOpen
    this.onOpenChange = onOpenChange
    this.controlledQuery = controlledQuery
    this.onQueryChange = onQueryChange
}
