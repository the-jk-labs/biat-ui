package com.biat.ui

import com.biat.ui.core.focus.shouldReturnFocus
import com.biat.ui.core.state.AccordionState
import com.biat.ui.core.state.AccordionType
import com.biat.ui.core.state.AvatarState
import com.biat.ui.core.state.AvatarStatus
import com.biat.ui.core.state.CollapsibleState
import com.biat.ui.core.state.DialogState
import com.biat.ui.core.state.MenuState
import com.biat.ui.core.state.PopoverState
import com.biat.ui.core.state.RadioGroupState
import com.biat.ui.core.state.SelectState
import com.biat.ui.core.state.SheetDetent
import com.biat.ui.core.state.SheetSettle
import com.biat.ui.core.state.SheetState
import com.biat.ui.core.state.SliderState
import com.biat.ui.core.state.TabMove
import com.biat.ui.core.state.ToggleGroupState
import com.biat.ui.core.state.ToggleGroupType
import com.biat.ui.core.state.ToggleState
import com.biat.ui.core.state.ToggleValue
import com.biat.ui.core.state.ToolbarState
import com.biat.ui.core.state.coerceAndSnap
import com.biat.ui.core.state.fractionToValue
import com.biat.ui.core.state.isOn
import com.biat.ui.core.state.next
import com.biat.ui.core.state.resolveSheetSettle
import com.biat.ui.core.state.resolveTabIndex
import com.biat.ui.core.state.resolveToolbarIndex
import com.biat.ui.core.state.TabsState
import com.biat.ui.core.state.TooltipState
import com.biat.ui.core.state.valueToFraction
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
    fun tabs_resolveIndex_wraps() {
        assertEquals(1, resolveTabIndex(0, 3, TabMove.Next))
        assertEquals(0, resolveTabIndex(2, 3, TabMove.Next))
        assertEquals(2, resolveTabIndex(0, 3, TabMove.Previous))
        assertEquals(0, resolveTabIndex(0, 3, TabMove.First))
        assertEquals(2, resolveTabIndex(0, 3, TabMove.Last))
    }

    @Test
    fun tabs_resolveIndex_clampsAndSingle() {
        assertEquals(0, resolveTabIndex(9, 3, TabMove.Next))
        assertEquals(2, resolveTabIndex(-4, 3, TabMove.Previous))
        assertEquals(0, resolveTabIndex(0, 1, TabMove.Next))
        assertEquals(0, resolveTabIndex(0, 1, TabMove.Last))
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
    fun select_queryFiltersOpensAndResetsHighlight() {
        val queries = mutableListOf<String>()
        val state = SelectState<String>(onQueryChange = { queries.add(it) })
        val options = listOf("Kotlin", "Java", "Kotlin Multiplatform", "Rust")
        assertEquals(options, state.filteredOptions(options))
        state.highlightedIndex = 2
        state.setQuery("kotlin")
        assertEquals("kotlin", state.query)
        assertTrue(state.isOpen)
        assertEquals(-1, state.highlightedIndex)
        assertEquals(
            listOf("Kotlin", "Kotlin Multiplatform"),
            state.filteredOptions(options),
        )
        assertEquals(listOf("kotlin"), queries)
        state.setQuery("kotlin") // no-op, no duplicate event
        assertEquals(listOf("kotlin"), queries)
        state.clearQuery()
        assertEquals("", state.query)
        assertEquals(options, state.filteredOptions(options))
    }

    @Test
    fun select_controlledQuery_notifiesWithoutMutating() {
        val events = mutableListOf<String>()
        val state = SelectState<String>(
            controlledQuery = "a",
            onQueryChange = { events.add(it) },
        )
        state.setQuery("ab")
        assertEquals("a", state.query)
        assertEquals(listOf("ab"), events)
        state.controlledQuery = "ab"
        assertEquals("ab", state.query)
    }

    @Test
    fun select_typeaheadMatchCyclesAndWraps() {
        val state = SelectState<String>()
        val options = listOf("Apple", "Apricot", "Banana")
        assertTrue(state.moveHighlightToMatch(options, { it }, "a"))
        assertEquals(0, state.highlightedIndex)
        assertTrue(state.moveHighlightToMatch(options, { it }, "a"))
        assertEquals(1, state.highlightedIndex)
        assertTrue(state.moveHighlightToMatch(options, { it }, "b"))
        assertEquals(2, state.highlightedIndex)
        assertFalse(state.moveHighlightToMatch(options, { it }, "z"))
        assertEquals(2, state.highlightedIndex)
        assertFalse(state.moveHighlightToMatch(emptyList(), { it }, "a"))
    }

    @Test
    fun select_closeResetsHighlight() {
        val state = SelectState<String>()
        state.open()
        state.highlightedIndex = 1
        state.close()
        assertEquals(-1, state.highlightedIndex)
        assertFalse(state.isOpen)
    }

    @Test
    fun menu_highlightSkipsDisabled() {
        val state = MenuState(itemCount = 4, disabledIndices = setOf(1, 2))
        state.open()
        state.moveHighlight(1)
        assertEquals(0, state.highlightedIndex)
        state.moveHighlight(1)
        assertEquals(3, state.highlightedIndex)
        state.moveHighlight(1)
        assertEquals(0, state.highlightedIndex)
        state.moveHighlight(-1)
        assertEquals(3, state.highlightedIndex)
        state.moveHighlight(-1)
        assertEquals(0, state.highlightedIndex)
    }

    @Test
    fun menu_highlightSnapsPastDisabled() {
        val state = MenuState(itemCount = 3, disabledIndices = setOf(0, 1))
        state.open()
        state.highlight(0) // Home lands on first enabled
        assertEquals(2, state.highlightedIndex)
        state.highlight(2)
        assertEquals(2, state.highlightedIndex)
        assertTrue(state.isEnabled(2))
        assertFalse(state.isEnabled(0))
    }

    @Test
    fun menu_allDisabled_neverHighlights() {
        val state = MenuState(itemCount = 2, disabledIndices = setOf(0, 1))
        state.open()
        state.moveHighlight(1)
        assertEquals(-1, state.highlightedIndex)
        state.highlight(0)
        assertEquals(-1, state.highlightedIndex)
    }

    @Test
    fun sheet_settlePositionalStepsAndDismisses() {
        // From Half, small drags snap back.
        assertEquals(
            SheetSettle.Snap(SheetDetent.Half),
            resolveSheetSettle(SheetDetent.Half, dragPx = 100f, sheetHeightPx = 1000f, velocityPxPerSec = 0f),
        )
        // Down 30% steps one stop down; down 60% dismisses.
        assertEquals(
            SheetSettle.Snap(SheetDetent.Peek),
            resolveSheetSettle(SheetDetent.Half, dragPx = 300f, sheetHeightPx = 1000f, velocityPxPerSec = 0f),
        )
        assertEquals(
            SheetSettle.Dismiss,
            resolveSheetSettle(SheetDetent.Half, dragPx = 600f, sheetHeightPx = 1000f, velocityPxPerSec = 0f),
        )
        // Down from the lowest stop dismisses past the step line.
        assertEquals(
            SheetSettle.Dismiss,
            resolveSheetSettle(SheetDetent.Peek, dragPx = 300f, sheetHeightPx = 1000f, velocityPxPerSec = 0f),
        )
        // Up 30% steps one stop up; Full stays Full.
        assertEquals(
            SheetSettle.Snap(SheetDetent.Full),
            resolveSheetSettle(SheetDetent.Half, dragPx = -300f, sheetHeightPx = 1000f, velocityPxPerSec = 0f),
        )
        assertEquals(
            SheetSettle.Snap(SheetDetent.Full),
            resolveSheetSettle(SheetDetent.Full, dragPx = -300f, sheetHeightPx = 1000f, velocityPxPerSec = 0f),
        )
    }

    @Test
    fun sheet_settleFlingsMoveOneStop() {
        assertEquals(
            SheetSettle.Snap(SheetDetent.Full),
            resolveSheetSettle(SheetDetent.Half, dragPx = 0f, sheetHeightPx = 1000f, velocityPxPerSec = -2000f),
        )
        assertEquals(
            SheetSettle.Snap(SheetDetent.Peek),
            resolveSheetSettle(SheetDetent.Half, dragPx = 0f, sheetHeightPx = 1000f, velocityPxPerSec = 2000f),
        )
        assertEquals(
            SheetSettle.Dismiss,
            resolveSheetSettle(SheetDetent.Peek, dragPx = 0f, sheetHeightPx = 1000f, velocityPxPerSec = 2000f),
        )
        // Slow drags below the fling line stay positional.
        assertEquals(
            SheetSettle.Snap(SheetDetent.Half),
            resolveSheetSettle(SheetDetent.Half, dragPx = 0f, sheetHeightPx = 1000f, velocityPxPerSec = 1499f),
        )
    }

    @Test
    fun sheet_settleHonorsEnabledDetents() {
        val two = setOf(SheetDetent.Peek, SheetDetent.Full)
        assertEquals(
            SheetSettle.Snap(SheetDetent.Full),
            resolveSheetSettle(SheetDetent.Peek, enabled = two, dragPx = -300f, sheetHeightPx = 1000f, velocityPxPerSec = 0f),
        )
        assertEquals(
            SheetSettle.Snap(SheetDetent.Peek),
            resolveSheetSettle(SheetDetent.Full, enabled = two, dragPx = 300f, sheetHeightPx = 1000f, velocityPxPerSec = 0f),
        )
        assertEquals(
            SheetSettle.Snap(SheetDetent.Peek),
            resolveSheetSettle(SheetDetent.Peek, enabled = setOf(SheetDetent.Peek), dragPx = 0f, sheetHeightPx = 1000f, velocityPxPerSec = 0f),
        )
    }

    @Test
    fun sheet_detentNotifiesAndMapsExpandCollapse() {
        val events = mutableListOf<SheetDetent>()
        val state = SheetState(onDetentChange = { events.add(it) })
        assertEquals(SheetDetent.Half, state.detent)
        assertFalse(state.isExpanded)
        state.expand()
        assertTrue(state.isExpanded)
        state.collapse()
        assertFalse(state.isExpanded)
        assertEquals(SheetDetent.Half, state.detent)
        state.snapTo(SheetDetent.Half) // no-op, no duplicate event
        assertEquals(listOf(SheetDetent.Full, SheetDetent.Half), events)
        state.open(SheetDetent.Peek)
        assertEquals(SheetDetent.Peek, state.detent)
        assertTrue(state.isOpen)
    }

    @Test
    fun focusReturn_onlyOnOpenToClose() {
        assertTrue(shouldReturnFocus(wasOpen = true, isOpen = false))
        assertFalse(shouldReturnFocus(wasOpen = false, isOpen = false))
        assertFalse(shouldReturnFocus(wasOpen = false, isOpen = true))
        assertFalse(shouldReturnFocus(wasOpen = true, isOpen = true))
    }

    @Test
    fun collapsible_toggleNotifies() {
        val events = mutableListOf<Boolean>()
        val state = CollapsibleState(onExpandedChange = { events.add(it) })
        assertFalse(state.isExpanded)
        state.toggle()
        assertTrue(state.isExpanded)
        state.setExpanded(true) // no-op, no duplicate event
        state.collapse()
        assertFalse(state.isExpanded)
        assertEquals(listOf(true, false), events)
    }

    @Test
    fun collapsible_controlledNotifiesWithoutMutating() {
        val events = mutableListOf<Boolean>()
        val state = CollapsibleState(
            controlledExpanded = false,
            onExpandedChange = { events.add(it) },
        )
        state.expand()
        assertFalse(state.isExpanded)
        assertEquals(listOf(true), events)
    }

    @Test
    fun accordion_singleCollapsible() {
        val events = mutableListOf<List<Any?>>()
        val state = AccordionState(onOpenChange = { events.add(it) })
        state.select("a")
        assertTrue(state.isOpen("a"))
        state.select("b")
        assertFalse(state.isOpen("a"))
        assertTrue(state.isOpen("b"))
        state.select("b")
        assertFalse(state.isOpen("b"))
        assertEquals(listOf(listOf("a"), listOf("b"), emptyList<Any>()), events)
    }

    @Test
    fun accordion_singleNonCollapsibleKeepsOneOpen() {
        val state = AccordionState(collapsible = false)
        state.select("a")
        state.select("a")
        assertTrue(state.isOpen("a"))
        assertEquals(listOf("a"), state.openValues)
    }

    @Test
    fun accordion_multipleTogglesMembership() {
        val state = AccordionState(type = AccordionType.Multiple)
        state.select("a")
        state.select("b")
        assertTrue(state.isOpen("a"))
        assertTrue(state.isOpen("b"))
        state.select("a")
        assertFalse(state.isOpen("a"))
        assertTrue(state.isOpen("b"))
    }

    @Test
    fun toggle_triStateAndDisabled() {
        val events = mutableListOf<ToggleValue>()
        val state = ToggleState(
            initialValue = ToggleValue.Indeterminate,
            onValueChange = { events.add(it) },
        )
        assertFalse(state.isOn)
        assertTrue(state.isIndeterminate)
        assertEquals(ToggleValue.On, ToggleValue.Indeterminate.next())
        state.toggle()
        assertTrue(state.isOn)
        state.enabled = false
        state.toggle()
        assertTrue(state.isOn)
        assertEquals(listOf(ToggleValue.On), events)
        assertTrue(ToggleValue.On.isOn())
        assertFalse(ToggleValue.Off.isOn())
    }

    @Test
    fun toggle_controlledNotifiesWithoutMutating() {
        val events = mutableListOf<ToggleValue>()
        val state = ToggleState(
            controlledValue = ToggleValue.Off,
            onValueChange = { events.add(it) },
        )
        state.toggle()
        assertFalse(state.isOn)
        assertEquals(listOf(ToggleValue.On), events)
    }

    @Test
    fun radioGroup_selectNoDeselect() {
        var last: Any? = "none"
        val state = RadioGroupState(
            initialSelected = "a",
            onSelectedChange = { last = it },
        )
        assertTrue(state.isSelected("a"))
        state.select("a") // no-op, no duplicate event
        assertEquals("none", last)
        state.select("b")
        assertTrue(state.isSelected("b"))
        assertEquals("b", last)
        state.enabled = false
        state.select("c")
        assertTrue(state.isSelected("b"))
    }

    @Test
    fun slider_clampsSnapsAndNotifies() {
        val events = mutableListOf<Float>()
        val state = SliderState(
            initialValue = 0f,
            valueRange = 0f..10f,
            step = 2f,
            onValueChange = { events.add(it) },
        )
        state.setValue(3f) // snaps to 4
        assertEquals(4f, state.value)
        state.setValue(4f) // no-op, no duplicate event
        state.setValue(99f) // clamps to max
        assertEquals(10f, state.value)
        state.increase()
        assertEquals(10f, state.value) // already at max
        state.toMin()
        assertEquals(0f, state.value)
        state.toMax()
        assertEquals(10f, state.value)
        assertEquals(listOf(4f, 10f, 0f, 10f), events)
    }

    @Test
    fun slider_controlledNotifiesWithoutMutating() {
        val events = mutableListOf<Float>()
        val state = SliderState(
            controlledValue = 2f,
            valueRange = 0f..10f,
            step = 1f,
            onValueChange = { events.add(it) },
        )
        state.increase()
        assertEquals(2f, state.value)
        assertEquals(listOf(3f), events)
        state.controlledValue = 3f
        assertEquals(3f, state.value)
    }

    @Test
    fun slider_fractionMathRoundTrips() {
        assertEquals(0.5f, valueToFraction(5f, 0f..10f))
        assertEquals(0f, valueToFraction(-5f, 0f..10f))
        assertEquals(1f, valueToFraction(99f, 0f..10f))
        assertEquals(5f, fractionToValue(0.5f, 0f..10f))
        assertEquals(0f, fractionToValue(-1f, 0f..10f))
        assertEquals(4f, coerceAndSnap(3f, 0f..10f, 2f))
        assertEquals(3.5f, coerceAndSnap(3.5f, 0f..10f, 0f))
    }

    @Test
    fun toggleGroup_singleDeselect() {
        val events = mutableListOf<List<Any?>>()
        val state = ToggleGroupState(onPressedChange = { events.add(it) })
        state.toggle("a")
        assertTrue(state.isPressed("a"))
        state.toggle("b")
        assertTrue(state.isPressed("b"))
        assertEquals(listOf("b"), state.pressedValues)
        state.toggle("b")
        assertTrue(state.pressedValues.isEmpty())
        assertEquals(
            listOf(listOf("a"), listOf("b"), emptyList<Any>()),
            events,
        )
    }

    @Test
    fun toggleGroup_singleNonDeselectableKeepsOne() {
        val state = ToggleGroupState(allowDeselect = false)
        state.toggle("a")
        state.toggle("a")
        assertTrue(state.isPressed("a"))
    }

    @Test
    fun toggleGroup_multipleTogglesMembership() {
        val state = ToggleGroupState(type = ToggleGroupType.Multiple)
        state.toggle("a")
        state.toggle("b")
        assertTrue(state.isPressed("a"))
        assertTrue(state.isPressed("b"))
        state.toggle("a")
        assertEquals(listOf("b"), state.pressedValues)
        state.enabled = false
        state.toggle("c")
        assertEquals(listOf("b"), state.pressedValues)
    }

    @Test
    fun toolbar_moveSkipsDisabledAndWraps() {
        assertEquals(2, resolveToolbarIndex(0, 3, 1, disabled = setOf(1)))
        assertEquals(0, resolveToolbarIndex(2, 3, 1))
        assertEquals(2, resolveToolbarIndex(0, 3, -1))
        assertEquals(-1, resolveToolbarIndex(0, 2, 1, disabled = setOf(0, 1)))
        assertEquals(-1, resolveToolbarIndex(0, 0, 1))
    }

    @Test
    fun toolbar_stateFocusMoves() {
        val state = ToolbarState(itemCount = 3, disabledIndices = setOf(1))
        state.move(1)
        assertEquals(2, state.focusedIndex)
        state.move(1)
        assertEquals(0, state.focusedIndex)
        state.moveToLast()
        assertEquals(2, state.focusedIndex)
        state.moveToFirst()
        assertEquals(0, state.focusedIndex)
    }

    @Test
    fun avatar_fallbackUntilLoaded() {
        val events = mutableListOf<AvatarStatus>()
        val state = AvatarState(onStatusChange = { events.add(it) })
        assertTrue(state.showFallback)
        state.markLoaded()
        assertEquals(AvatarStatus.Loaded, state.status)
        assertEquals(listOf(AvatarStatus.Loaded), events)
        state.markLoaded() // no-op, no duplicate event
        state.markError()
        assertTrue(state.showFallback)
        state.reset()
        assertEquals(AvatarStatus.Loading, state.status)
    }

    @Test
    fun menu_toggleCloseResetsHighlight() {
        val state = MenuState(itemCount = 3)
        state.open()
        state.highlight(1)
        state.toggle() // closing via toggle resets like close()
        assertFalse(state.isOpen)
        assertEquals(-1, state.highlightedIndex)
        state.toggle() // reopening starts with no highlight
        assertTrue(state.isOpen)
        assertEquals(-1, state.highlightedIndex)
    }

    @Test
    fun select_toggleCloseResetsHighlight() {
        val state = SelectState<String>()
        state.open()
        state.highlightedIndex = 1
        state.toggle()
        assertFalse(state.isOpen)
        assertEquals(-1, state.highlightedIndex)
    }

    @Test
    fun select_controlledQuery_leavesTransientAlone() {
        val events = mutableListOf<String>()
        val state = SelectState<String>(
            controlledQuery = "a",
            onQueryChange = { events.add(it) },
        )
        state.highlightedIndex = 1
        state.setQuery("ab") // owner has not accepted; transient stays
        assertEquals("a", state.query)
        assertEquals(1, state.highlightedIndex)
        assertFalse(state.isOpen)
        assertEquals(listOf("ab"), events)
        state.controlledQuery = "ab"
        assertEquals("ab", state.query)
    }

    @Test
    fun select_moveHighlightFromNoneAndCommit() {
        val state = SelectState<String>()
        val options = listOf("a", "b", "c")
        state.moveHighlight(-1, options.size) // Up from none lands last
        assertEquals(2, state.highlightedIndex)
        state.moveHighlight(-1, options.size)
        assertEquals(1, state.highlightedIndex)
        state.moveHighlight(1, options.size)
        assertEquals(2, state.highlightedIndex)
        state.moveHighlight(1, options.size) // wraps
        assertEquals(0, state.highlightedIndex)
        assertEquals("a", state.commitValue(options))
        state.close()
        assertEquals(null, state.commitValue(options))
        state.highlightFirst(options.size)
        assertEquals(0, state.highlightedIndex)
        state.highlightLast(options.size)
        assertEquals(2, state.highlightedIndex)
        state.moveHighlight(1, 0) // empty list is a no-op
        assertEquals(2, state.highlightedIndex)
    }

    @Test
    fun menu_itemCountShrinkClampsHighlightKeepsOpen() {
        val state = MenuState(itemCount = 3)
        state.open()
        state.highlight(2)
        state.itemCount = 1 // async options shrink; menu stays open
        assertTrue(state.isOpen)
        assertEquals(-1, state.highlightedIndex)
    }

    @Test
    fun toolbar_itemCountShrinkClampsFocus() {
        val state = ToolbarState(itemCount = 3)
        state.focus(2)
        assertEquals(2, state.focusedIndex)
        state.itemCount = 1
        assertEquals(0, state.focusedIndex)
    }

    @Test
    fun slider_updateConfigResnapsUncontrolled() {
        val state = SliderState(
            initialValue = 8f,
            valueRange = 0f..10f,
            step = 1f,
        )
        assertEquals(8f, state.value)
        state.updateConfig(0f..5f, 1f)
        assertEquals(5f, state.value)
    }
}
