package com.lutukai.simpletodoapp.ui.addedittodo

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
class AddEditTodoMviViewModelTest {

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
    fun `initial state is add mode`() = runTest {
        val viewModel = AddEditTodoMviViewModel(repository)
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
        coEvery { repository.getTodoById(1L) } returns todo

        val viewModel = AddEditTodoMviViewModel(repository)
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
        coEvery { repository.getTodoById(any()) } returns createTodo()

        val viewModel = AddEditTodoMviViewModel(repository)
        viewModel.onIntent(AddEditTodoIntent.LoadTodo(1L))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `loadTodo handles not found`() = runTest {
        coEvery { repository.getTodoById(1L) } returns null

        val viewModel = AddEditTodoMviViewModel(repository)

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
        coEvery { repository.getTodoById(any()) } throws RuntimeException("Database error")

        val viewModel = AddEditTodoMviViewModel(repository)

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
        val viewModel = AddEditTodoMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(AddEditTodoIntent.UpdateTitle("New Title"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.title).isEqualTo("New Title")
    }

    @Test
    fun `updateDescription updates state`() = runTest {
        val viewModel = AddEditTodoMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(AddEditTodoIntent.UpdateDescription("New Description"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.description).isEqualTo("New Description")
    }

    @Test
    fun `updateCompleted updates state`() = runTest {
        val viewModel = AddEditTodoMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(AddEditTodoIntent.UpdateCompleted(true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isCompleted).isTrue()
    }

    @Test
    fun `saveTodo creates new todo in add mode`() = runTest {
        coEvery { repository.insertTodo(any()) } returns Unit

        val viewModel = AddEditTodoMviViewModel(repository)
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
            repository.insertTodo(match {
                it.id == null && it.title == "New Task" && it.description == "Task Description"
            })
        }
    }

    @Test
    fun `saveTodo updates existing todo in edit mode`() = runTest {
        val existingTodo = createTodo(1L, "Old Title", "Old Desc")
        coEvery { repository.getTodoById(1L) } returns existingTodo
        coEvery { repository.insertTodo(any()) } returns Unit

        val viewModel = AddEditTodoMviViewModel(repository)
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
            repository.insertTodo(match {
                it.id == 1L && it.title == "Updated Title"
            })
        }
    }

    @Test
    fun `saveTodo shows error when title is blank`() = runTest {
        val viewModel = AddEditTodoMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(AddEditTodoIntent.SaveTodo)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(AddEditTodoSideEffect.ShowError::class.java)
            assertThat((effect as AddEditTodoSideEffect.ShowError).message).isEqualTo("Title is required")
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { repository.insertTodo(any()) }
    }

    @Test
    fun `saveTodo handles error`() = runTest {
        coEvery { repository.insertTodo(any()) } throws RuntimeException("Save failed")

        val viewModel = AddEditTodoMviViewModel(repository)
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
        val viewModel = AddEditTodoMviViewModel(repository)
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
        val viewModel = AddEditTodoMviViewModel(repository)
        viewModel.onIntent(AddEditTodoIntent.UpdateTitle("Test"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isSaveEnabled).isTrue()
    }

    @Test
    fun `isSaveEnabled returns false when title is blank`() = runTest {
        val viewModel = AddEditTodoMviViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isSaveEnabled).isFalse()
    }
}
