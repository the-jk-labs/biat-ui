package com.biat.ui

import com.biat.ui.core.state.DialogState
import com.biat.ui.core.state.MenuState
import com.biat.ui.core.state.SelectState
import com.biat.ui.core.state.SheetState
import com.biat.ui.core.state.TabsState
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
}
