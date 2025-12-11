package com.lutukai.simpletodoapp.ui.tododetail

import android.os.Looper
import androidx.core.os.bundleOf
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lutukai.simpletodoapp.R
import com.lutukai.simpletodoapp.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Robolectric tests for TodoDetailDialog.
 * Tests dialog UI behavior without needing an emulator.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(
    application = HiltTestApplication::class,
    sdk = [33]
)
class TodoDetailDialogTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `dialog displays title text view`() {
        launchFragmentInHiltContainer<TodoDetailDialog>(
            fragmentArgs = bundleOf(TodoDetailDialog.ARG_TODO_ID to 1L)
        )

        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()))
    }

    @Test
    fun `dialog displays description text view`() {
        launchFragmentInHiltContainer<TodoDetailDialog>(
            fragmentArgs = bundleOf(TodoDetailDialog.ARG_TODO_ID to 1L)
        )

        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.tvDescription)).check(matches(isDisplayed()))
    }

    @Test
    fun `dialog displays completion switch`() {
        launchFragmentInHiltContainer<TodoDetailDialog>(
            fragmentArgs = bundleOf(TodoDetailDialog.ARG_TODO_ID to 1L)
        )

        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.switchCompleted)).check(matches(isDisplayed()))
    }

    @Test
    fun `dialog displays edit button`() {
        launchFragmentInHiltContainer<TodoDetailDialog>(
            fragmentArgs = bundleOf(TodoDetailDialog.ARG_TODO_ID to 1L)
        )

        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.btnEdit)).check(matches(isDisplayed()))
    }

    @Test
    fun `dialog displays close button`() {
        launchFragmentInHiltContainer<TodoDetailDialog>(
            fragmentArgs = bundleOf(TodoDetailDialog.ARG_TODO_ID to 1L)
        )

        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.btnClose)).check(matches(isDisplayed()))
    }

    @Test
    fun `dialog displays created at date`() {
        launchFragmentInHiltContainer<TodoDetailDialog>(
            fragmentArgs = bundleOf(TodoDetailDialog.ARG_TODO_ID to 1L)
        )

        shadowOf(Looper.getMainLooper()).idle()

        // Check visibility attribute since view may not have content yet (todo not loaded)
        onView(withId(R.id.tvCreatedAt)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }
}