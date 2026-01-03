package com.lutukai.simpletodoapp.ui.todolist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.ui.TestTodoFactory
import com.lutukai.simpletodoapp.ui.features.todolist.TodoListContent
import com.lutukai.simpletodoapp.ui.features.todolist.TodoListIntent
import com.lutukai.simpletodoapp.ui.features.todolist.TodoListState
import com.lutukai.simpletodoapp.ui.theme.SimpleTodoAppTheme
import com.lutukai.simpletodoapp.ui.util.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class TodoListContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `displays loading indicator when isLoading true`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoListContent(
                    state = TodoListState(isLoading = true),
                    onIntent = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.LOADING_INDICATOR)
            .assertIsDisplayed()
    }

    @Test
    fun `displays empty state when no todos and not loading`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoListContent(
                    state = TodoListState(todos = emptyList(), isLoading = false),
                    onIntent = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.EMPTY_STATE)
            .assertIsDisplayed()
    }

    @Test
    fun `displays todo list when todos exist`() {
        val todos = TestTodoFactory.createTodoList(3)

        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoListContent(
                    state = TodoListState(todos = todos, filteredTodos = todos, isLoading = false),
                    onIntent = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.TODO_LIST)
            .assertIsDisplayed()

        // Verify each todo item is displayed
        todos.forEach { todo ->
            composeTestRule
                .onNodeWithText(todo.title)
                .assertIsDisplayed()
        }
    }

    @Test
    fun `displays search bar`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoListContent(
                    state = TodoListState(),
                    onIntent = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.SEARCH_BAR)
            .assertIsDisplayed()
    }

    @Test
    fun `displays both filter tabs`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoListContent(
                    state = TodoListState(),
                    onIntent = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.TAB_ALL)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(TestTags.TAB_COMPLETED)
            .assertIsDisplayed()
    }

    @Test
    fun `search bar triggers UpdateSearchQuery intent on text input`() {
        var capturedIntent: TodoListIntent? = null

        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoListContent(
                    state = TodoListState(),
                    onIntent = { capturedIntent = it }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.SEARCH_BAR)
            .performTextInput("test")

        assertThat(capturedIntent).isInstanceOf(TodoListIntent.UpdateSearchQuery::class.java)
    }

    @Test
    fun `clicking COMPLETED tab triggers UpdateFilter intent`() {
        var capturedIntent: TodoListIntent? = null

        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoListContent(
                    state = TodoListState(),
                    onIntent = { capturedIntent = it }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.TAB_COMPLETED)
            .performClick()

        assertThat(capturedIntent).isEqualTo(
            TodoListIntent.UpdateFilter(TodoListState.TodoFilter.COMPLETED)
        )
    }

    @Test
    fun `clicking ALL tab triggers UpdateFilter intent`() {
        var capturedIntent: TodoListIntent? = null

        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoListContent(
                    state = TodoListState(filter = TodoListState.TodoFilter.COMPLETED),
                    onIntent = { capturedIntent = it }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.TAB_ALL)
            .performClick()

        assertThat(capturedIntent).isEqualTo(
            TodoListIntent.UpdateFilter(TodoListState.TodoFilter.ALL)
        )
    }
}
