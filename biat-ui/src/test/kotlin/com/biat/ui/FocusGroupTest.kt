package com.biat.ui

import androidx.compose.ui.focus.FocusRequester
import com.biat.ui.core.focus.FocusEntry
import com.biat.ui.core.focus.FocusGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/** Focus-group transitions do not request UI focus until the composition effect. */
class FocusGroupTest {
    @Test
    fun movementSkipsDisabledAndWraps() {
        val group = FocusGroup()
        val first = FocusEntry(FocusRequester())
        val disabled = FocusEntry(FocusRequester()).apply { enabled = false }
        val last = FocusEntry(FocusRequester())
        group.entries.addAll(listOf(first, disabled, last))
        group.current = first
        var activated = 0
        last.onNavigate = { activated++ }
        group.move(1)
        assertSame(last, group.current)
        assertSame(last, group.pending)
        assertEquals(1, activated)
        group.move(1)
        assertSame(first, group.current)
        group.move(-1)
        assertSame(last, group.current)
    }

    @Test
    fun nonLoopingMovementStopsAtEdge() {
        val group = FocusGroup().apply { loop = false }
        val first = FocusEntry(FocusRequester())
        val last = FocusEntry(FocusRequester())
        group.entries.addAll(listOf(first, last))
        group.current = last
        group.move(1)
        assertSame(last, group.current)
        group.move(-1)
        group.move(-1)
        assertSame(first, group.current)
    }

    @Test
    fun removedOrDisabledCurrentFallsBackToEnabledEntry() {
        val group = FocusGroup()
        val first = FocusEntry(FocusRequester())
        val last = FocusEntry(FocusRequester())
        group.entries.addAll(listOf(first, last))
        group.current = last
        last.enabled = false
        assertSame(first, group.tabStop())
        first.enabled = false
        assertNull(group.tabStop())
    }
}
