package com.biat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.biat.ui.core.state.SelectState
import com.biat.ui.core.state.rememberSelectState
import com.biat.ui.primitives.select.Select
import org.junit.Rule
import org.junit.Test

/**
 * On-device certification for Select depth (v0.3.0): query filtering,
 * empty/loading slots, clearable selection, controlled query.
 */
class SelectDepthTest {
    @get:Rule
    val rule = createComposeRule()

    private fun harness(
        state: SelectState<String>,
        options: List<String> = listOf("Kotlin", "Java", "Rust"),
        isLoading: Boolean = false,
    ) {
        rule.setContent {
            Select(
                layout = { content -> Column { content() } },
                state = state,
                options = options,
                isLoading = isLoading,
                loading = { BasicText("Loading...") },
                empty = { BasicText("No matches") },
                trigger = { selected -> BasicText(selected ?: "Pick language") },
                item = { value, _, _ -> BasicText(value) },
            )
        }
    }

    @Test
    fun query_filtersRenderedOptions() {
        val state = SelectState<String>()
        harness(state)
        rule.runOnIdle { state.open() }
        rule.onNodeWithText("Java").assertExists()
        rule.runOnIdle { state.setQuery("rust") }
        rule.onNodeWithText("Rust").assertExists()
        rule.onNodeWithText("Java").assertDoesNotExist()
        rule.onNodeWithText("Kotlin").assertDoesNotExist()
    }

    @Test
    fun query_withNoMatch_showsEmptySlot() {
        val state = SelectState<String>()
        harness(state)
        rule.runOnIdle { state.open() }
        rule.runOnIdle { state.setQuery("zzz") }
        rule.onNodeWithText("No matches").assertExists()
        rule.onNodeWithText("Java").assertDoesNotExist()
    }

    @Test
    fun loading_showsLoadingSlotInsteadOfOptions() {
        val state = SelectState<String>()
        harness(state, isLoading = true)
        rule.runOnIdle { state.open() }
        rule.onNodeWithText("Loading...").assertExists()
        rule.onNodeWithText("Java").assertDoesNotExist()
    }

    @Test
    fun clearSelection_resetsTrigger() {
        val state = SelectState<String>()
        harness(state)
        rule.runOnIdle { state.select("Java") }
        rule.onNodeWithText("Java").assertExists()
        rule.runOnIdle { state.clearSelection() }
        rule.onNodeWithText("Pick language").assertExists()
    }

    @Test
    fun controlledQuery_filtersWithoutOwningText() {
        rule.setContent {
            var query by remember { mutableStateOf("j") }
            val state =
                rememberSelectState<String>(
                    controlledQuery = query,
                    onQueryChange = { query = it },
                )
            Select(
                layout = { content -> Column { content() } },
                state = state,
                options = listOf("Kotlin", "Java", "Rust"),
                empty = { BasicText("No matches") },
                trigger = { selected -> BasicText(selected ?: "Pick") },
                item = { value, _, _ -> BasicText(value) },
            )
            androidx.compose.runtime.LaunchedEffect(Unit) { state.open() }
        }
        rule.onNodeWithText("Java").assertExists()
        rule.onNodeWithText("Kotlin").assertDoesNotExist()
    }
}
