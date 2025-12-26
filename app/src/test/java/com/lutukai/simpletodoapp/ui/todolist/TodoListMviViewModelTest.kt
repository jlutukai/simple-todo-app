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
class TodoListMviViewModelTest {

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
        isCompleted: Boolean = false
    ) = Todo(
        id = id,
        title = title,
        description = "Test Description",
        isCompleted = isCompleted,
        completedAt = if (isCompleted) System.currentTimeMillis() else null,
        createdAt = System.currentTimeMillis()
    )

    @Test
    fun `init loads todos on creation`() = runTest {
        val todos = listOf(createTodo(1, "Todo 1"), createTodo(2, "Todo 2"))
        every { repository.getAllTodos() } returns flowOf(todos)

        val viewModel = TodoListMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.todos).hasSize(2)
        assertThat(viewModel.state.value.isLoading).isFalse()
        assertThat(viewModel.state.value.error).isNull()
    }

    @Test
    fun `loadTodos sets loading to false after completion`() = runTest {
        every { repository.getAllTodos() } returns flowOf(emptyList())

        val viewModel = TodoListMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `loadTodos handles error`() = runTest {
        every { repository.getAllTodos() } returns flow { throw RuntimeException("Network error") }

        val viewModel = TodoListMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.error).isEqualTo("Network error")
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `updateSearchQuery updates state`() = runTest {
        every { repository.getAllTodos() } returns flowOf(emptyList())

        val viewModel = TodoListMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(TodoListIntent.UpdateSearchQuery("test query"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.searchQuery).isEqualTo("test query")
    }

    @Test
    fun `updateFilter updates state`() = runTest {
        every { repository.getAllTodos() } returns flowOf(emptyList())

        val viewModel = TodoListMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(TodoListIntent.UpdateFilter(TodoListState.TodoFilter.COMPLETED))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.filter).isEqualTo(TodoListState.TodoFilter.COMPLETED)
    }

    @Test
    fun `toggleComplete updates todo`() = runTest {
        val todo = createTodo(1, "Test", isCompleted = false)
        every { repository.getAllTodos() } returns flowOf(listOf(todo))
        coEvery { repository.updateTodo(any()) } returns Unit

        val viewModel = TodoListMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(TodoListIntent.ToggleComplete(todo))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.updateTodo(match {
                it.id == 1L && it.isCompleted == true && it.completedAt != null
            })
        }
    }

    @Test
    fun `toggleComplete handles error and shows snackbar`() = runTest {
        val todo = createTodo(1, "Test")
        every { repository.getAllTodos() } returns flowOf(listOf(todo))
        coEvery { repository.updateTodo(any()) } throws RuntimeException("Update failed")

        val viewModel = TodoListMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(TodoListIntent.ToggleComplete(todo))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(TodoListSideEffect.ShowSnackbar::class.java)
            assertThat((effect as TodoListSideEffect.ShowSnackbar).message).isEqualTo("Update failed")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteTodo calls repository and shows snackbar with undo`() = runTest {
        val todo = createTodo(1, "Test")
        every { repository.getAllTodos() } returns flowOf(listOf(todo))
        coEvery { repository.deleteTodo(any()) } returns Unit

        val viewModel = TodoListMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(TodoListIntent.DeleteTodo(todo))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(TodoListSideEffect.ShowSnackbar::class.java)
            val snackbar = effect as TodoListSideEffect.ShowSnackbar
            assertThat(snackbar.message).isEqualTo("Task deleted")
            assertThat(snackbar.actionLabel).isEqualTo("Undo")
            assertThat(snackbar.todo).isEqualTo(todo)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { repository.deleteTodo(todo) }
    }

    @Test
    fun `deleteTodo handles error`() = runTest {
        val todo = createTodo(1, "Test")
        every { repository.getAllTodos() } returns flowOf(listOf(todo))
        coEvery { repository.deleteTodo(any()) } throws RuntimeException("Delete failed")

        val viewModel = TodoListMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(TodoListIntent.DeleteTodo(todo))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(TodoListSideEffect.ShowSnackbar::class.java)
            assertThat((effect as TodoListSideEffect.ShowSnackbar).message).isEqualTo("Delete failed")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `undoDelete inserts todo back`() = runTest {
        val todo = createTodo(1, "Test")
        every { repository.getAllTodos() } returns flowOf(emptyList())
        coEvery { repository.insertTodo(any()) } returns Unit

        val viewModel = TodoListMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(TodoListIntent.UndoDelete(todo))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.insertTodo(todo) }
    }

    @Test
    fun `undoDelete handles error`() = runTest {
        val todo = createTodo(1, "Test")
        every { repository.getAllTodos() } returns flowOf(emptyList())
        coEvery { repository.insertTodo(any()) } throws RuntimeException("Restore failed")

        val viewModel = TodoListMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(TodoListIntent.UndoDelete(todo))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(TodoListSideEffect.ShowSnackbar::class.java)
            assertThat((effect as TodoListSideEffect.ShowSnackbar).message).isEqualTo("Restore failed")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addNewTodo sends navigation effect`() = runTest {
        every { repository.getAllTodos() } returns flowOf(emptyList())

        val viewModel = TodoListMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(TodoListIntent.AddNewTodo)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(TodoListSideEffect.NavigateToAddTodo)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `openTodoDetail sends navigation effect with todoId`() = runTest {
        every { repository.getAllTodos() } returns flowOf(emptyList())

        val viewModel = TodoListMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(TodoListIntent.OpenTodoDetail(42L))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(TodoListSideEffect.NavigateToDetail::class.java)
            assertThat((effect as TodoListSideEffect.NavigateToDetail).todoId).isEqualTo(42L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filteredTodos filters by completed state`() = runTest {
        val todos = listOf(
            createTodo(1, "Todo 1", isCompleted = false),
            createTodo(2, "Todo 2", isCompleted = true),
            createTodo(3, "Todo 3", isCompleted = false)
        )
        every { repository.getAllTodos() } returns flowOf(todos)

        val viewModel = TodoListMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // All filter - shows all
        assertThat(viewModel.state.value.filteredTodos).hasSize(3)

        // Completed filter - shows only completed
        viewModel.onIntent(TodoListIntent.UpdateFilter(TodoListState.TodoFilter.COMPLETED))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.filteredTodos).hasSize(1)
        assertThat(viewModel.state.value.filteredTodos[0].id).isEqualTo(2L)
    }

    @Test
    fun `filteredTodos filters by search query`() = runTest {
        val todos = listOf(
            createTodo(1, "Buy groceries"),
            createTodo(2, "Call mom"),
            createTodo(3, "Buy flowers")
        )
        every { repository.getAllTodos() } returns flowOf(todos)

        val viewModel = TodoListMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(TodoListIntent.UpdateSearchQuery("Buy"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.filteredTodos).hasSize(2)
    }
}
