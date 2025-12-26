package com.lutukai.simpletodoapp.ui.tododetail

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodoDetailMviViewModelTest {

    private lateinit var repository: TodoRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createTodo(
        id: Long = 1L,
        title: String = "Test Todo",
        description: String = "Test Description",
        isCompleted: Boolean = false
    ) = Todo(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        completedAt = if (isCompleted) System.currentTimeMillis() else null,
        createdAt = System.currentTimeMillis()
    )

    @Test
    fun `initial state has no todo`() = runTest {
        val viewModel = TodoDetailMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.todo).isNull()
        assertThat(viewModel.state.value.isLoading).isFalse()
        assertThat(viewModel.state.value.error).isNull()
    }

    @Test
    fun `loadTodo sets state with todo data`() = runTest {
        val todo = createTodo(1L, "Test Title", "Test Desc")
        coEvery { repository.getTodoById(1L) } returns todo

        val viewModel = TodoDetailMviViewModel(repository)
        viewModel.onIntent(TodoDetailIntent.LoadTodo(1L))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.todo).isEqualTo(todo)
        assertThat(viewModel.state.value.isLoading).isFalse()
        assertThat(viewModel.state.value.error).isNull()
    }

    @Test
    fun `loadTodo sets loading to false after completion`() = runTest {
        coEvery { repository.getTodoById(any()) } returns createTodo()

        val viewModel = TodoDetailMviViewModel(repository)
        viewModel.onIntent(TodoDetailIntent.LoadTodo(1L))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `loadTodo handles not found`() = runTest {
        coEvery { repository.getTodoById(1L) } returns null

        val viewModel = TodoDetailMviViewModel(repository)

        viewModel.effect.test {
            viewModel.onIntent(TodoDetailIntent.LoadTodo(1L))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(TodoDetailSideEffect.ShowError::class.java)
            assertThat((effect as TodoDetailSideEffect.ShowError).message).isEqualTo("Todo not found")
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(viewModel.state.value.error).isEqualTo("Todo not found")
        assertThat(viewModel.state.value.todo).isNull()
    }

    @Test
    fun `loadTodo handles error`() = runTest {
        coEvery { repository.getTodoById(any()) } throws RuntimeException("Database error")

        val viewModel = TodoDetailMviViewModel(repository)

        viewModel.effect.test {
            viewModel.onIntent(TodoDetailIntent.LoadTodo(1L))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(TodoDetailSideEffect.ShowError::class.java)
            assertThat((effect as TodoDetailSideEffect.ShowError).message).isEqualTo("Database error")
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(viewModel.state.value.error).isEqualTo("Database error")
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `toggleComplete updates todo to completed`() = runTest {
        val todo = createTodo(1L, "Test", isCompleted = false)
        coEvery { repository.getTodoById(1L) } returns todo
        coEvery { repository.updateTodo(any()) } returns Unit

        val viewModel = TodoDetailMviViewModel(repository)
        viewModel.onIntent(TodoDetailIntent.LoadTodo(1L))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(TodoDetailIntent.ToggleComplete(true))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.updateTodo(match {
                it.id == 1L && it.isCompleted && it.completedAt != null
            })
        }
        assertThat(viewModel.state.value.todo?.isCompleted).isTrue()
    }

    @Test
    fun `toggleComplete updates todo to not completed`() = runTest {
        val todo = createTodo(1L, "Test", isCompleted = true)
        coEvery { repository.getTodoById(1L) } returns todo
        coEvery { repository.updateTodo(any()) } returns Unit

        val viewModel = TodoDetailMviViewModel(repository)
        viewModel.onIntent(TodoDetailIntent.LoadTodo(1L))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(TodoDetailIntent.ToggleComplete(false))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.updateTodo(match {
                it.id == 1L && !it.isCompleted && it.completedAt == null
            })
        }
        assertThat(viewModel.state.value.todo?.isCompleted).isFalse()
    }

    @Test
    fun `toggleComplete handles error`() = runTest {
        val todo = createTodo(1L, "Test")
        coEvery { repository.getTodoById(1L) } returns todo
        coEvery { repository.updateTodo(any()) } throws RuntimeException("Update failed")

        val viewModel = TodoDetailMviViewModel(repository)
        viewModel.onIntent(TodoDetailIntent.LoadTodo(1L))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(TodoDetailIntent.ToggleComplete(true))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(TodoDetailSideEffect.ShowError::class.java)
            assertThat((effect as TodoDetailSideEffect.ShowError).message).isEqualTo("Update failed")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleComplete does nothing when no todo loaded`() = runTest {
        val viewModel = TodoDetailMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(TodoDetailIntent.ToggleComplete(true))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { repository.updateTodo(any()) }
    }

    @Test
    fun `editClicked sends navigate effect with todoId`() = runTest {
        val todo = createTodo(1L, "Test")
        coEvery { repository.getTodoById(1L) } returns todo

        val viewModel = TodoDetailMviViewModel(repository)
        viewModel.onIntent(TodoDetailIntent.LoadTodo(1L))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(TodoDetailIntent.EditClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(TodoDetailSideEffect.NavigateToEdit::class.java)
            assertThat((effect as TodoDetailSideEffect.NavigateToEdit).todoId).isEqualTo(1L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `editClicked does nothing when no todo loaded`() = runTest {
        val viewModel = TodoDetailMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(TodoDetailIntent.EditClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismiss sends dismiss effect`() = runTest {
        val viewModel = TodoDetailMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(TodoDetailIntent.Dismiss)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(TodoDetailSideEffect.Dismiss)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
