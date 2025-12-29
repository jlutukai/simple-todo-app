package com.lutukai.simpletodoapp.ui.todolist

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
import com.lutukai.simpletodoapp.ui.theme.SimpleTodoAppTheme
import com.lutukai.simpletodoapp.ui.util.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class TodoItemComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testTodo = TestTodoFactory.createTodo(id = 1L, title = "Test Todo")

    @Test
    fun `displays todo title`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoItem(
                    todo = testTodo,
                    onToggleComplete = {},
                    onDelete = {},
                    onClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Test Todo")
            .assertIsDisplayed()
    }

    @Test
    fun `displays checkbox`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoItem(
                    todo = testTodo,
                    onToggleComplete = {},
                    onDelete = {},
                    onClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.todoCheckbox(1L))
            .assertIsDisplayed()
    }

    @Test
    fun `checkbox is unchecked when todo not completed`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoItem(
                    todo = testTodo.copy(isCompleted = false),
                    onToggleComplete = {},
                    onDelete = {},
                    onClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.todoCheckbox(1L))
            .assertIsOff()
    }

    @Test
    fun `checkbox is checked when todo completed`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoItem(
                    todo = testTodo.copy(isCompleted = true),
                    onToggleComplete = {},
                    onDelete = {},
                    onClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.todoCheckbox(1L))
            .assertIsOn()
    }

    @Test
    fun `clicking checkbox calls onToggleComplete`() {
        var toggleCalled = false

        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoItem(
                    todo = testTodo,
                    onToggleComplete = { toggleCalled = true },
                    onDelete = {},
                    onClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.todoCheckbox(1L))
            .performClick()

        assertThat(toggleCalled).isTrue()
    }

    @Test
    fun `clicking delete button calls onDelete`() {
        var deleteCalled = false

        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoItem(
                    todo = testTodo,
                    onToggleComplete = {},
                    onDelete = { deleteCalled = true },
                    onClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.todoDeleteButton(1L))
            .performClick()

        assertThat(deleteCalled).isTrue()
    }

    @Test
    fun `clicking card calls onClick`() {
        var clickCalled = false

        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                TodoItem(
                    todo = testTodo,
                    onToggleComplete = {},
                    onDelete = {},
                    onClick = { clickCalled = true }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.todoItem(1L))
            .performClick()

        assertThat(clickCalled).isTrue()
    }
}
