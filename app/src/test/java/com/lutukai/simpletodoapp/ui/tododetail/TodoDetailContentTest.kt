package com.lutukai.simpletodoapp.ui.tododetail

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.ui.TestTodoFactory
import com.lutukai.simpletodoapp.ui.features.tododetail.TodoDetailContent
import com.lutukai.simpletodoapp.ui.features.tododetail.TodoDetailIntent
import com.lutukai.simpletodoapp.ui.features.tododetail.TodoDetailState
import com.lutukai.simpletodoapp.ui.theme.SimpleTodoAppTheme
import com.lutukai.simpletodoapp.ui.util.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class TodoDetailContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testTodo = TestTodoFactory.createTodo(
        id = 1L,
        title = "Test Todo Title",
        description = "Test Description"
    )

    @Test
    fun `displays loading indicator when isLoading true`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoDetailContent(
                    state = TodoDetailState(isLoading = true),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.DETAIL_LOADING)
            .assertIsDisplayed()
    }

    @Test
    fun `displays edit button`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoDetailContent(
                    state = TodoDetailState(todo = testTodo),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.DETAIL_EDIT_BUTTON)
            .assertIsDisplayed()
    }

    @Test
    fun `displays close button`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoDetailContent(
                    state = TodoDetailState(todo = testTodo),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.DETAIL_CLOSE_BUTTON)
            .assertIsDisplayed()
    }

    @Test
    fun `displays todo title`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoDetailContent(
                    state = TodoDetailState(todo = testTodo),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.DETAIL_TITLE)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Test Todo Title")
            .assertIsDisplayed()
    }

    @Test
    fun `displays completion switch`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoDetailContent(
                    state = TodoDetailState(todo = testTodo),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.DETAIL_COMPLETED_SWITCH)
            .assertIsDisplayed()
    }

    @Test
    fun `completion switch is off when todo not completed`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoDetailContent(
                    state = TodoDetailState(todo = testTodo.copy(isCompleted = false)),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.DETAIL_COMPLETED_SWITCH)
            .assertIsOff()
    }

    @Test
    fun `completion switch is on when todo completed`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoDetailContent(
                    state = TodoDetailState(todo = testTodo.copy(isCompleted = true)),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.DETAIL_COMPLETED_SWITCH)
            .assertIsOn()
    }

    @Test
    fun `displays notes section`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoDetailContent(
                    state = TodoDetailState(todo = testTodo),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.DETAIL_NOTES)
            .assertIsDisplayed()
    }

    @Test
    fun `displays dash when description is empty`() {
        val todoWithEmptyDescription = testTodo.copy(description = "")

        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoDetailContent(
                    state = TodoDetailState(todo = todoWithEmptyDescription),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithText("-")
            .assertIsDisplayed()
    }

    @Test
    fun `clicking edit button triggers EditClicked intent`() {
        var capturedIntent: TodoDetailIntent? = null

        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoDetailContent(
                    state = TodoDetailState(todo = testTodo),
                    onIntent = { capturedIntent = it },
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.DETAIL_EDIT_BUTTON)
            .performClick()

        assertThat(capturedIntent).isEqualTo(TodoDetailIntent.EditClicked)
    }

    @Test
    fun `clicking close button triggers Dismiss intent`() {
        var capturedIntent: TodoDetailIntent? = null

        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoDetailContent(
                    state = TodoDetailState(todo = testTodo),
                    onIntent = { capturedIntent = it },
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.DETAIL_CLOSE_BUTTON)
            .performClick()

        assertThat(capturedIntent).isEqualTo(TodoDetailIntent.Dismiss)
    }
}
