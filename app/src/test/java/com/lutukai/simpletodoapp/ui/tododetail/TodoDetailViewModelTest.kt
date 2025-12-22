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
class TodoDetailViewModelTest {

    private lateinit var repository: TodoRepository
    private lateinit var viewModel: TodoDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = TodoDetailViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ============== LOAD TODO TESTS ==============

    @Test
    fun `loadTodo displays todo on success`() = runTest {
        val todo = createTodo(1, "Test Todo")
        coEvery { repository.getTodoById(1L) } returns todo

        viewModel.loadTodo(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.todo?.title).isEqualTo("Test Todo")
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun `loadTodo shows error when todo not found`() = runTest {
        coEvery { repository.getTodoById(1L) } returns null

        viewModel.events.test {
            viewModel.loadTodo(1L)
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(TodoDetailEvent.ShowError::class.java)
            assertThat((event as TodoDetailEvent.ShowError).message).isEqualTo("Todo not found")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTodo shows error on failure`() = runTest {
        val error = RuntimeException("Database error")
        coEvery { repository.getTodoById(1L) } throws error

        viewModel.events.test {
            viewModel.loadTodo(1L)
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(TodoDetailEvent.ShowError::class.java)
            assertThat((event as TodoDetailEvent.ShowError).message).isEqualTo("Database error")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTodo shows default error message when error message is null`() = runTest {
        val error = RuntimeException()
        coEvery { repository.getTodoById(1L) } throws error

        viewModel.events.test {
            viewModel.loadTodo(1L)
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(TodoDetailEvent.ShowError::class.java)
            assertThat((event as TodoDetailEvent.ShowError).message).isEqualTo("An Unknown Error Occurred")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============== TOGGLE COMPLETE TESTS ==============

    @Test
    fun `toggleComplete updates todo to completed state`() = runTest {
        val todo = createTodo(1, "Test", isCompleted = false)
        coEvery { repository.getTodoById(1L) } returns todo
        coEvery { repository.updateTodo(any()) } returns Unit

        // First load the todo
        viewModel.loadTodo(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then toggle complete
        viewModel.toggleComplete(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.updateTodo(match {
                it.id == 1L && it.isCompleted && it.completedAt != null
            })
        }
    }

    @Test
    fun `toggleComplete updates todo to incomplete state`() = runTest {
        val todo = createTodo(1, "Test", isCompleted = true, completedAt = 123456L)
        coEvery { repository.getTodoById(1L) } returns todo
        coEvery { repository.updateTodo(any()) } returns Unit

        // First load the todo
        viewModel.loadTodo(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then toggle complete
        viewModel.toggleComplete(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.updateTodo(match {
                it.id == 1L && !it.isCompleted && it.completedAt == null
            })
        }
    }

    @Test
    fun `toggleComplete updates UI state on success`() = runTest {
        val todo = createTodo(1, "Test", isCompleted = false)
        coEvery { repository.getTodoById(1L) } returns todo
        coEvery { repository.updateTodo(any()) } returns Unit

        viewModel.loadTodo(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertThat(initialState.todo?.isCompleted).isFalse()

            viewModel.toggleComplete(true)
            testDispatcher.scheduler.advanceUntilIdle()

            val updatedState = awaitItem()
            assertThat(updatedState.todo?.isCompleted).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleComplete shows error on failure`() = runTest {
        val todo = createTodo(1, "Test", isCompleted = false)
        coEvery { repository.getTodoById(1L) } returns todo
        coEvery { repository.updateTodo(any()) } throws RuntimeException("Update failed")

        viewModel.loadTodo(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.events.test {
            viewModel.toggleComplete(true)
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(TodoDetailEvent.ShowError::class.java)
            assertThat((event as TodoDetailEvent.ShowError).message).isEqualTo("Update failed")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleComplete shows default error message when error message is null`() = runTest {
        val todo = createTodo(1, "Test", isCompleted = false)
        coEvery { repository.getTodoById(1L) } returns todo
        coEvery { repository.updateTodo(any()) } throws RuntimeException()

        viewModel.loadTodo(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.events.test {
            viewModel.toggleComplete(true)
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(TodoDetailEvent.ShowError::class.java)
            assertThat((event as TodoDetailEvent.ShowError).message).isEqualTo("Failed to update task")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleComplete does nothing when currentTodo is null`() = runTest {
        // Don't load any todo, so currentTodo remains null
        viewModel.toggleComplete(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { repository.updateTodo(any()) }
    }

    // ============== ON EDIT CLICKED TESTS ==============

    @Test
    fun `onEditClicked emits NavigateToEdit event with correct ID`() = runTest {
        val todo = createTodo(42, "Test Todo")
        coEvery { repository.getTodoById(42L) } returns todo

        viewModel.loadTodo(42L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.events.test {
            viewModel.onEditClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(TodoDetailEvent.NavigateToEdit::class.java)
            assertThat((event as TodoDetailEvent.NavigateToEdit).todoId).isEqualTo(42L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onEditClicked does nothing when currentTodo is null`() = runTest {
        // Don't load any todo, so currentTodo remains null
        viewModel.events.test {
            viewModel.onEditClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============== HELPER FUNCTIONS ==============

    private fun createTodo(
        id: Long = 0,
        title: String,
        description: String = "",
        isCompleted: Boolean = false,
        completedAt: Long? = null,
        createdAt: Long = System.currentTimeMillis()
    ): Todo {
        return Todo(
            id = id,
            title = title,
            description = description,
            isCompleted = isCompleted,
            completedAt = completedAt,
            createdAt = createdAt
        )
    }
}