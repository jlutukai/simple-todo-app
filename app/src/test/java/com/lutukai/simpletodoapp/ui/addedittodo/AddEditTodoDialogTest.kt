package com.lutukai.simpletodoapp.ui.addedittodo

import android.os.Looper
import androidx.core.os.bundleOf
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lutukai.simpletodoapp.R
import com.lutukai.simpletodoapp.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Robolectric tests for AddEditTodoDialog.
 * Tests dialog UI behavior without needing an emulator.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(
    application = HiltTestApplication::class,
    sdk = [33]
)
class AddEditTodoDialogTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `dialog opens in ADD mode with correct title`() {
        launchFragmentInHiltContainer<AddEditTodoDialog>()

        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.tvDialogTitle))
            .check(matches(withText(R.string.add_todo_title)))
    }

    @Test
    fun `dialog opens in EDIT mode with correct title`() {
        launchFragmentInHiltContainer<AddEditTodoDialog>(
            fragmentArgs = bundleOf(AddEditTodoDialog.ARG_TODO_ID to 1L)
        )

        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.tvDialogTitle))
            .check(matches(withText(R.string.edit_todo_title)))
    }

    @Test
    fun `dialog displays title input field`() {
        launchFragmentInHiltContainer<AddEditTodoDialog>()

        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.etTitle)).check(matches(isDisplayed()))
    }

    @Test
    fun `dialog displays description input field`() {
        launchFragmentInHiltContainer<AddEditTodoDialog>()

        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.etDescription)).check(matches(isDisplayed()))
    }

    @Test
    fun `dialog displays save button`() {
        launchFragmentInHiltContainer<AddEditTodoDialog>()

        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.btnSave)).check(matches(isDisplayed()))
        onView(withId(R.id.btnSave)).check(matches(isEnabled()))
    }

    @Test
    fun `dialog displays cancel button`() {
        launchFragmentInHiltContainer<AddEditTodoDialog>()

        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.btnCancel)).check(matches(isDisplayed()))
    }

    @Test
    fun `completed toggle is hidden in ADD mode`() {
        launchFragmentInHiltContainer<AddEditTodoDialog>()

        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.completedToggleContainer))
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun `completed toggle is visible in EDIT mode`() {
        launchFragmentInHiltContainer<AddEditTodoDialog>(
            fragmentArgs = bundleOf(AddEditTodoDialog.ARG_TODO_ID to 1L)
        )

        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.completedToggleContainer))
            .check(matches(isDisplayed()))
    }
}