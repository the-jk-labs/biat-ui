package com.biat.ui

import com.biat.ui.core.focus.shouldReturnFocus
import com.biat.ui.core.state.DialogState
import com.biat.ui.core.state.MenuState
import com.biat.ui.core.state.PopoverState
import com.biat.ui.core.state.SelectState
import com.biat.ui.core.state.SheetState
import com.biat.ui.core.state.TabsState
import com.biat.ui.core.state.TooltipState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StateMachinesTest {

    @Test
    fun dialog_openCloseToggle() {
        val events = mutableListOf<Boolean>()
        val state = DialogState(onOpenChange = { events.add(it) })
        assertFalse(state.isOpen)
        state.open()
        assertTrue(state.isOpen)
        state.toggle()
        assertFalse(state.isOpen)
        state.setOpen(false) // no-op, no duplicate event
        assertEquals(listOf(true, false), events)
    }

    @Test
    fun menu_highlightWraps() {
        val state = MenuState(itemCount = 3)
        state.open()
        state.moveHighlight(1)
        assertEquals(0, state.highlightedIndex)
        state.moveHighlight(1)
        assertEquals(1, state.highlightedIndex)
        state.highlight(2)
        state.moveHighlight(1)
        assertEquals(0, state.highlightedIndex)
        state.close()
        assertEquals(-1, state.highlightedIndex)
        assertFalse(state.isOpen)
    }

    @Test
    fun select_commitsAndCloses() {
        var selected: String? = "none"
        val state = SelectState<String>(onSelectedChange = { selected = it })
        state.open()
        state.select("a")
        assertEquals("a", state.selected)
        assertEquals("a", selected)
        assertFalse(state.isOpen)
        state.clearSelection()
        assertEquals(null, state.selected)
    }

    @Test
    fun tabs_select() {
        var last: Any? = null
        val state = TabsState(initialSelected = "a", onSelectedChange = { last = it })
        assertTrue(state.isSelected("a"))
        state.select("b")
        assertTrue(state.isSelected("b"))
        assertEquals("b", last)
    }

    @Test
    fun sheet_expandCollapse() {
        val state = SheetState()
        state.open(expanded = true)
        assertTrue(state.isOpen)
        assertTrue(state.isExpanded)
        state.collapse()
        assertFalse(state.isExpanded)
        assertTrue(state.isOpen)
        state.close()
        assertFalse(state.isOpen)
    }

    @Test
    fun dialog_controlled_notifiesWithoutMutating() {
        val events = mutableListOf<Boolean>()
        val state = DialogState(
            controlledOpen = false,
            onOpenChange = { events.add(it) },
        )
        state.open()
        assertFalse(state.isOpen) // caller owns the value; no mutation
        assertEquals(listOf(true), events)
        // Controlled notifications are at-least-once: the value is still
        // false (caller has not reflected it), so requesting again notifies.
        state.open()
        assertEquals(listOf(true, true), events)

        // Caller reflects the value back, as rememberDialogState does.
        state.controlledOpen = true
        assertTrue(state.isOpen)
        state.close()
        assertTrue(state.isOpen)
        assertEquals(listOf(true, true, false), events)
        state.controlledOpen = false
        assertFalse(state.isOpen)
    }

    @Test
    fun menu_controlled_closeResetsHighlightButNotifies() {
        val events = mutableListOf<Boolean>()
        val state = MenuState(
            itemCount = 3,
            controlledOpen = true,
            onOpenChange = { events.add(it) },
        )
        assertTrue(state.isOpen)
        state.highlight(2)
        state.close()
        assertEquals(-1, state.highlightedIndex) // transient state resets
        assertTrue(state.isOpen) // openness stays until the caller updates
        assertEquals(listOf(false), events)
    }

    @Test
    fun tooltip_controlled_showHide() {
        val events = mutableListOf<Boolean>()
        val state = TooltipState(
            controlledVisible = false,
            onVisibleChange = { events.add(it) },
        )
        state.show()
        assertFalse(state.isVisible)
        assertEquals(listOf(true), events)
        state.hide() // no-op: already not visible
        assertEquals(listOf(true), events)
        state.controlledVisible = true
        assertTrue(state.isVisible)
        state.hide()
        assertTrue(state.isVisible)
        assertEquals(listOf(true, false), events)
    }

    @Test
    fun popover_select_sheet_controlled_notifyOnly() {
        val popoverEvents = mutableListOf<Boolean>()
        val popover = PopoverState(
            controlledOpen = true,
            onOpenChange = { popoverEvents.add(it) },
        )
        popover.close()
        assertTrue(popover.isOpen)
        assertEquals(listOf(false), popoverEvents)

        val selectEvents = mutableListOf<Boolean>()
        val select = SelectState<String>(
            controlledOpen = false,
            onOpenChange = { selectEvents.add(it) },
        )
        select.toggle()
        assertFalse(select.isOpen)
        assertEquals(listOf(true), selectEvents)

        val sheetEvents = mutableListOf<Boolean>()
        val sheet = SheetState(
            controlledOpen = false,
            onOpenChange = { sheetEvents.add(it) },
        )
        sheet.open(expanded = true)
        assertFalse(sheet.isOpen)
        assertTrue(sheet.isExpanded) // expansion stays internal
        assertEquals(listOf(true), sheetEvents)
    }

    @Test
    fun focusReturn_onlyOnOpenToClose() {
        assertTrue(shouldReturnFocus(wasOpen = true, isOpen = false))
        assertFalse(shouldReturnFocus(wasOpen = false, isOpen = false))
        assertFalse(shouldReturnFocus(wasOpen = false, isOpen = true))
        assertFalse(shouldReturnFocus(wasOpen = true, isOpen = true))
    }
}
