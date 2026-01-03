package com.lutukai.simpletodoapp.ui.addedittodo

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.usecases.GetTodoByIdUseCase
import com.lutukai.simpletodoapp.domain.usecases.InsertTodoUseCase
import com.lutukai.simpletodoapp.ui.features.addedittodo.AddEditTodoIntent
import com.lutukai.simpletodoapp.ui.features.addedittodo.AddEditTodoMviViewModel
import com.lutukai.simpletodoapp.ui.features.addedittodo.AddEditTodoSideEffect
import com.lutukai.simpletodoapp.util.Result
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
class AddEditTodoMviViewModelTest {

    private lateinit var getTodoByIdUseCase: GetTodoByIdUseCase
    private lateinit var insertTodoUseCase: InsertTodoUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTodoByIdUseCase = mockk(relaxed = true)
        insertTodoUseCase = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AddEditTodoMviViewModel(
        getTodoByIdUseCase,
        insertTodoUseCase
    )

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
    fun `initial state is add mode`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isEditMode).isFalse()
        assertThat(viewModel.state.value.title).isEmpty()
        assertThat(viewModel.state.value.description).isEmpty()
        assertThat(viewModel.state.value.isCompleted).isFalse()
        assertThat(viewModel.state.value.todo).isNull()
    }

    @Test
    fun `loadTodo sets edit mode with data`() = runTest {
        val todo = createTodo(1L, "Test Title", "Test Desc", false)
        coEvery { getTodoByIdUseCase(1L) } returns Result.Success(todo)

        val viewModel = createViewModel()
        viewModel.onIntent(AddEditTodoIntent.LoadTodo(1L))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isEditMode).isTrue()
        assertThat(viewModel.state.value.title).isEqualTo("Test Title")
        assertThat(viewModel.state.value.description).isEqualTo("Test Desc")
        assertThat(viewModel.state.value.todo).isEqualTo(todo)
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `loadTodo sets loading to false after completion`() = runTest {
        coEvery { getTodoByIdUseCase(any()) } returns Result.Success(createTodo())

        val viewModel = createViewModel()
        viewModel.onIntent(AddEditTodoIntent.LoadTodo(1L))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `loadTodo handles not found`() = runTest {
        coEvery { getTodoByIdUseCase(1L) } returns Result.Success(null)

        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onIntent(AddEditTodoIntent.LoadTodo(1L))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(AddEditTodoSideEffect.ShowError::class.java)
            assertThat((effect as AddEditTodoSideEffect.ShowError).message).isEqualTo("Todo not found")
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(viewModel.state.value.error).isEqualTo("Todo not found")
    }

    @Test
    fun `loadTodo handles error`() = runTest {
        coEvery { getTodoByIdUseCase(any()) } returns Result.Failure(RuntimeException("Database error"))

        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onIntent(AddEditTodoIntent.LoadTodo(1L))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(AddEditTodoSideEffect.ShowError::class.java)
            assertThat((effect as AddEditTodoSideEffect.ShowError).message).isEqualTo("Database error")
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(viewModel.state.value.error).isEqualTo("Database error")
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `updateTitle updates state`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(AddEditTodoIntent.UpdateTitle("New Title"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.title).isEqualTo("New Title")
    }

    @Test
    fun `updateDescription updates state`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(AddEditTodoIntent.UpdateDescription("New Description"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.description).isEqualTo("New Description")
    }

    @Test
    fun `updateCompleted updates state`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(AddEditTodoIntent.UpdateCompleted(true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isCompleted).isTrue()
    }

    @Test
    fun `saveTodo creates new todo in add mode`() = runTest {
        coEvery { insertTodoUseCase(any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        viewModel.onIntent(AddEditTodoIntent.UpdateTitle("New Task"))
        viewModel.onIntent(AddEditTodoIntent.UpdateDescription("Task Description"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(AddEditTodoIntent.SaveTodo)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(AddEditTodoSideEffect.SaveSuccess)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify {
            insertTodoUseCase(
                match {
                    it.id == null && it.title == "New Task" && it.description == "Task Description"
                }
            )
        }
    }

    @Test
    fun `saveTodo updates existing todo in edit mode`() = runTest {
        val existingTodo = createTodo(1L, "Old Title", "Old Desc")
        coEvery { getTodoByIdUseCase(1L) } returns Result.Success(existingTodo)
        coEvery { insertTodoUseCase(any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        viewModel.onIntent(AddEditTodoIntent.LoadTodo(1L))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(AddEditTodoIntent.UpdateTitle("Updated Title"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(AddEditTodoIntent.SaveTodo)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(AddEditTodoSideEffect.SaveSuccess)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify {
            insertTodoUseCase(
                match {
                    it.id == 1L && it.title == "Updated Title"
                }
            )
        }
    }

    @Test
    fun `saveTodo shows error when title is blank`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(AddEditTodoIntent.SaveTodo)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(AddEditTodoSideEffect.ShowError::class.java)
            assertThat((effect as AddEditTodoSideEffect.ShowError).message).isEqualTo("Title is required")
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { insertTodoUseCase(any()) }
    }

    @Test
    fun `saveTodo handles error`() = runTest {
        coEvery { insertTodoUseCase(any()) } returns Result.Failure(RuntimeException("Save failed"))

        val viewModel = createViewModel()
        viewModel.onIntent(AddEditTodoIntent.UpdateTitle("Test"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(AddEditTodoIntent.SaveTodo)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(AddEditTodoSideEffect.ShowError::class.java)
            assertThat((effect as AddEditTodoSideEffect.ShowError).message).isEqualTo("Save failed")
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(viewModel.state.value.error).isEqualTo("Save failed")
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `cancel sends dismiss effect`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(AddEditTodoIntent.Cancel)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(AddEditTodoSideEffect.Dismiss)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isSaveEnabled returns true when title not blank and not loading`() = runTest {
        val viewModel = createViewModel()
        viewModel.onIntent(AddEditTodoIntent.UpdateTitle("Test"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isSaveEnabled).isTrue()
    }

    @Test
    fun `isSaveEnabled returns false when title is blank`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isSaveEnabled).isFalse()
    }
}
