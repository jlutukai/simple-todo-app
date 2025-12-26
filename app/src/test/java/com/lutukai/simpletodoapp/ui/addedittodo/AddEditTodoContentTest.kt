package com.lutukai.simpletodoapp.ui.addedittodo

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.ui.theme.SimpleTodoAppTheme
import com.lutukai.simpletodoapp.ui.util.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class AddEditTodoContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `displays title input field`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                AddEditTodoContent(
                    state = AddEditTodoState(),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.ADD_EDIT_TITLE_FIELD)
            .assertIsDisplayed()
    }

    @Test
    fun `displays description input field`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                AddEditTodoContent(
                    state = AddEditTodoState(),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.ADD_EDIT_DESCRIPTION_FIELD)
            .assertIsDisplayed()
    }

    @Test
    fun `displays save button`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                AddEditTodoContent(
                    state = AddEditTodoState(),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.ADD_EDIT_SAVE_BUTTON)
            .assertIsDisplayed()
    }

    @Test
    fun `displays cancel button`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                AddEditTodoContent(
                    state = AddEditTodoState(),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.ADD_EDIT_CANCEL_BUTTON)
            .assertIsDisplayed()
    }

    @Test
    fun `hides completion switch in add mode`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                AddEditTodoContent(
                    state = AddEditTodoState(isEditMode = false),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.ADD_EDIT_COMPLETED_SWITCH)
            .assertDoesNotExist()
    }

    @Test
    fun `shows completion switch in edit mode`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                AddEditTodoContent(
                    state = AddEditTodoState(isEditMode = true),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.ADD_EDIT_COMPLETED_SWITCH)
            .assertIsDisplayed()
    }

    @Test
    fun `save button disabled when title empty`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                AddEditTodoContent(
                    state = AddEditTodoState(title = ""),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.ADD_EDIT_SAVE_BUTTON)
            .assertIsNotEnabled()
    }

    @Test
    fun `save button enabled when title not empty`() {
        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                AddEditTodoContent(
                    state = AddEditTodoState(title = "Test Title"),
                    onIntent = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.ADD_EDIT_SAVE_BUTTON)
            .assertIsEnabled()
    }

    @Test
    fun `clicking cancel button triggers Cancel intent`() {
        var capturedIntent: AddEditTodoIntent? = null

        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                AddEditTodoContent(
                    state = AddEditTodoState(),
                    onIntent = { capturedIntent = it },
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.ADD_EDIT_CANCEL_BUTTON)
            .performClick()

        assertThat(capturedIntent).isEqualTo(AddEditTodoIntent.Cancel)
    }

    @Test
    fun `clicking save button triggers SaveTodo intent`() {
        var capturedIntent: AddEditTodoIntent? = null

        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                AddEditTodoContent(
                    state = AddEditTodoState(title = "Test"),
                    onIntent = { capturedIntent = it },
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.ADD_EDIT_SAVE_BUTTON)
            .performClick()

        assertThat(capturedIntent).isEqualTo(AddEditTodoIntent.SaveTodo)
    }

    @Test
    fun `typing in title field triggers UpdateTitle intent`() {
        var capturedIntent: AddEditTodoIntent? = null

        composeTestRule.setContent {
            SimpleTodoAppTheme(dynamicColor = false) {
                AddEditTodoContent(
                    state = AddEditTodoState(),
                    onIntent = { capturedIntent = it },
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TestTags.ADD_EDIT_TITLE_FIELD)
            .performTextInput("Test")

        assertThat(capturedIntent).isInstanceOf(AddEditTodoIntent.UpdateTitle::class.java)
    }
}
