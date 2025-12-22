package com.lutukai.simpletodoapp.ui.todolist

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodoListViewModelTest {

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

    // ============== LOAD TODOS TESTS ==============

    @Test
    fun `loadTodos emits todos on success`() = runTest {
        val todos = listOf(createTodo(1, "Todo 1"), createTodo(2, "Todo 2"))
        every { repository.getAllTodos() } returns flowOf(todos)

        val viewModel = TodoListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.todos).hasSize(2)
            assertThat(state.isLoading).isFalse()
            assertThat(state.error).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTodos emits empty list when no todos`() = runTest {
        every { repository.getAllTodos() } returns flowOf(emptyList())

        val viewModel = TodoListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.todos).isEmpty()
            assertThat(state.isLoading).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTodos emits error on failure`() = runTest {
        val error = RuntimeException("Database error")
        every { repository.getAllTodos() } returns flow { throw error }

        val viewModel = TodoListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.error).isEqualTo("Database error")
            assertThat(state.isLoading).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============== ADD NEW TODO TESTS ==============

    @Test
    fun `addNewTodo emits NavigateToAddTodo event`() = runTest {
        every { repository.getAllTodos() } returns flowOf(emptyList())

        val viewModel = TodoListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.events.test {
            viewModel.addNewTodo()
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(TodoListEvent.NavigateToAddTodo::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============== OPEN TODO DETAIL TESTS ==============

    @Test
    fun `openTodoDetail emits NavigateToDetail event with correct ID`() = runTest {
        every { repository.getAllTodos() } returns flowOf(emptyList())

        val viewModel = TodoListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val todo = createTodo(42, "Test Todo")

        viewModel.events.test {
            viewModel.openTodoDetail(todo)
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(TodoListEvent.NavigateToDetail::class.java)
            assertThat((event as TodoListEvent.NavigateToDetail).todoId).isEqualTo(42L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============== TOGGLE COMPLETE TESTS ==============

    @Test
    fun `toggleComplete updates todo to completed state`() = runTest {
        every { repository.getAllTodos() } returns flowOf(emptyList())
        coEvery { repository.updateTodo(any()) } returns Unit

        val viewModel = TodoListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val todo = createTodo(1, "Test", isCompleted = false)
        viewModel.toggleComplete(todo)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.updateTodo(match {
                it.id == 1L && it.isCompleted && it.completedAt != null
            })
        }
    }

    @Test
    fun `toggleComplete updates todo to incomplete state`() = runTest {
        every { repository.getAllTodos() } returns flowOf(emptyList())
        coEvery { repository.updateTodo(any()) } returns Unit

        val viewModel = TodoListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val todo = createTodo(1, "Test", isCompleted = true, completedAt = 123456L)
        viewModel.toggleComplete(todo)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.updateTodo(match {
                it.id == 1L && !it.isCompleted && it.completedAt == null
            })
        }
    }

    @Test
    fun `toggleComplete emits error snackbar on failure`() = runTest {
        every { repository.getAllTodos() } returns flowOf(emptyList())
        coEvery { repository.updateTodo(any()) } throws RuntimeException("Update failed")

        val viewModel = TodoListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val todo = createTodo(1, "Test")

        viewModel.events.test {
            viewModel.toggleComplete(todo)
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(TodoListEvent.ShowSnackbar::class.java)
            assertThat((event as TodoListEvent.ShowSnackbar).message).isEqualTo("Update failed")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============== DELETE TODO TESTS ==============

    @Test
    fun `deleteTodo deletes and emits snackbar with undo action`() = runTest {
        every { repository.getAllTodos() } returns flowOf(emptyList())
        coEvery { repository.deleteTodo(any()) } returns Unit

        val viewModel = TodoListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val todo = createTodo(1, "Test")

        viewModel.events.test {
            viewModel.deleteTodo(todo)
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(TodoListEvent.ShowSnackbar::class.java)
            val snackbar = event as TodoListEvent.ShowSnackbar
            assertThat(snackbar.message).isEqualTo("Task deleted")
            assertThat(snackbar.actionLabel).isEqualTo("Undo")
            assertThat(snackbar.action).isNotNull()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { repository.deleteTodo(todo) }
    }

    @Test
    fun `deleteTodo emits error snackbar on failure`() = runTest {
        every { repository.getAllTodos() } returns flowOf(emptyList())
        coEvery { repository.deleteTodo(any()) } throws RuntimeException("Delete failed")

        val viewModel = TodoListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val todo = createTodo(1, "Test")

        viewModel.events.test {
            viewModel.deleteTodo(todo)
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(TodoListEvent.ShowSnackbar::class.java)
            assertThat((event as TodoListEvent.ShowSnackbar).message).isEqualTo("Delete failed")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============== UNDO DELETE TESTS ==============

    @Test
    fun `undoDelete re-inserts the todo`() = runTest {
        every { repository.getAllTodos() } returns flowOf(emptyList())
        coEvery { repository.insertTodo(any()) } returns Unit

        val viewModel = TodoListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val todo = createTodo(1, "Test")
        viewModel.undoDelete(todo)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.insertTodo(todo) }
    }

    @Test
    fun `undoDelete emits error snackbar on failure`() = runTest {
        every { repository.getAllTodos() } returns flowOf(emptyList())
        coEvery { repository.insertTodo(any()) } throws RuntimeException("Restore failed")

        val viewModel = TodoListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val todo = createTodo(1, "Test")

        viewModel.events.test {
            viewModel.undoDelete(todo)
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(TodoListEvent.ShowSnackbar::class.java)
            assertThat((event as TodoListEvent.ShowSnackbar).message).isEqualTo("Restore failed")
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