package com.lutukai.simpletodoapp.ui.todolist

import android.os.Looper
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
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
 * Robolectric tests for TodoListFragment.
 * Tests fragment UI behavior without needing an emulator.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(
    application = HiltTestApplication::class,
    sdk = [33]
)
class TodoListFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `fragment has recycler view`() {
        launchFragmentInHiltContainer<TodoListFragment>()

        // Let any pending UI operations complete
        shadowOf(Looper.getMainLooper()).idle()

        // RecyclerView exists but may be hidden when list is empty
        onView(withId(R.id.rvTodos)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun `fragment displays fab button`() {
        launchFragmentInHiltContainer<TodoListFragment>()

        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.fabAddTodo)).check(matches(isDisplayed()))
    }

    @Test
    fun `fragment displays tabs`() {
        launchFragmentInHiltContainer<TodoListFragment>()

        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.tabAll)).check(matches(isDisplayed()))
        onView(withId(R.id.tabCompleted)).check(matches(isDisplayed()))
    }

    @Test
    fun `fragment displays search field`() {
        launchFragmentInHiltContainer<TodoListFragment>()

        shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.etSearch)).check(matches(isDisplayed()))
    }
}