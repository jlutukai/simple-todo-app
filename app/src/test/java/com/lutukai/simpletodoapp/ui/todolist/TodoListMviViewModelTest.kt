package com.lutukai.simpletodoapp.ui.todolist

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.usecases.DeleteTodoUseCase
import com.lutukai.simpletodoapp.domain.usecases.GetAllTodosUseCase
import com.lutukai.simpletodoapp.domain.usecases.InsertTodoUseCase
import com.lutukai.simpletodoapp.domain.usecases.ToggleTodoCompleteUseCase
import com.lutukai.simpletodoapp.util.Result
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

    private lateinit var getAllTodosUseCase: GetAllTodosUseCase
    private lateinit var toggleTodoCompleteUseCase: ToggleTodoCompleteUseCase
    private lateinit var deleteTodoUseCase: DeleteTodoUseCase
    private lateinit var insertTodoUseCase: InsertTodoUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getAllTodosUseCase = mockk(relaxed = true)
        toggleTodoCompleteUseCase = mockk(relaxed = true)
        deleteTodoUseCase = mockk(relaxed = true)
        insertTodoUseCase = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = TodoListMviViewModel(
        getAllTodosUseCase,
        toggleTodoCompleteUseCase,
        deleteTodoUseCase,
        insertTodoUseCase
    )

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
        every { getAllTodosUseCase() } returns flowOf(todos)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.todos).hasSize(2)
        assertThat(viewModel.state.value.isLoading).isFalse()
        assertThat(viewModel.state.value.error).isNull()
    }

    @Test
    fun `loadTodos sets loading to false after completion`() = runTest {
        every { getAllTodosUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `loadTodos handles error`() = runTest {
        every { getAllTodosUseCase() } returns flow { throw RuntimeException("Network error") }

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.error).isEqualTo("Network error")
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `updateSearchQuery updates state`() = runTest {
        every { getAllTodosUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(TodoListIntent.UpdateSearchQuery("test query"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.searchQuery).isEqualTo("test query")
    }

    @Test
    fun `updateFilter updates state`() = runTest {
        every { getAllTodosUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(TodoListIntent.UpdateFilter(TodoListState.TodoFilter.COMPLETED))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.filter).isEqualTo(TodoListState.TodoFilter.COMPLETED)
    }

    @Test
    fun `toggleComplete calls use case`() = runTest {
        val todo = createTodo(1, "Test", isCompleted = false)
        val toggledTodo = todo.copy(isCompleted = true, completedAt = System.currentTimeMillis())
        every { getAllTodosUseCase() } returns flowOf(listOf(todo))
        coEvery { toggleTodoCompleteUseCase(any()) } returns Result.Success(toggledTodo)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(TodoListIntent.ToggleComplete(todo))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { toggleTodoCompleteUseCase(todo) }
    }

    @Test
    fun `toggleComplete handles error and shows snackbar`() = runTest {
        val todo = createTodo(1, "Test")
        every { getAllTodosUseCase() } returns flowOf(listOf(todo))
        coEvery { toggleTodoCompleteUseCase(any()) } returns Result.Failure(RuntimeException("Update failed"))

        val viewModel = createViewModel()
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
    fun `deleteTodo calls use case and shows snackbar with undo`() = runTest {
        val todo = createTodo(1, "Test")
        every { getAllTodosUseCase() } returns flowOf(listOf(todo))
        coEvery { deleteTodoUseCase(any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
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

        coVerify { deleteTodoUseCase(todo) }
    }

    @Test
    fun `deleteTodo handles error`() = runTest {
        val todo = createTodo(1, "Test")
        every { getAllTodosUseCase() } returns flowOf(listOf(todo))
        coEvery { deleteTodoUseCase(any()) } returns Result.Failure(RuntimeException("Delete failed"))

        val viewModel = createViewModel()
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
    fun `undoDelete calls insert use case`() = runTest {
        val todo = createTodo(1, "Test")
        every { getAllTodosUseCase() } returns flowOf(emptyList())
        coEvery { insertTodoUseCase(any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(TodoListIntent.UndoDelete(todo))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { insertTodoUseCase(todo) }
    }

    @Test
    fun `undoDelete handles error`() = runTest {
        val todo = createTodo(1, "Test")
        every { getAllTodosUseCase() } returns flowOf(emptyList())
        coEvery { insertTodoUseCase(any()) } returns Result.Failure(RuntimeException("Restore failed"))

        val viewModel = createViewModel()
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
        every { getAllTodosUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
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
        every { getAllTodosUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
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
        every { getAllTodosUseCase() } returns flowOf(todos)

        val viewModel = createViewModel()
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
        every { getAllTodosUseCase() } returns flowOf(todos)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(TodoListIntent.UpdateSearchQuery("Buy"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.filteredTodos).hasSize(2)
    }
}
