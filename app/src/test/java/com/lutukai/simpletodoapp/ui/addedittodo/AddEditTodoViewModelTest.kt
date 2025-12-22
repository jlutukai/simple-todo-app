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
class AddEditTodoViewModelTest {

    private lateinit var repository: TodoRepository
    private lateinit var viewModel: AddEditTodoViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = AddEditTodoViewModel(repository)
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
            assertThat(event).isInstanceOf(AddEditTodoEvent.ShowError::class.java)
            assertThat((event as AddEditTodoEvent.ShowError).message).isEqualTo("Todo not found")
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
            assertThat(event).isInstanceOf(AddEditTodoEvent.ShowError::class.java)
            assertThat((event as AddEditTodoEvent.ShowError).message).isEqualTo("Database error")
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
            assertThat(event).isInstanceOf(AddEditTodoEvent.ShowError::class.java)
            assertThat((event as AddEditTodoEvent.ShowError).message).isEqualTo("An Unknown Error Occurred")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============== SAVE TODO TESTS ==============

    @Test
    fun `saveTodo creates new todo when existingId is null`() = runTest {
        coEvery { repository.insertTodo(any()) } returns Unit

        viewModel.saveTodo(
            title = "New Todo",
            description = "Description",
            isCompleted = false,
            existingId = null
        )
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.insertTodo(match {
                it.id == null &&
                it.title == "New Todo" &&
                it.description == "Description" &&
                !it.isCompleted &&
                it.completedAt == null
            })
        }
    }

    @Test
    fun `saveTodo updates existing todo when existingId is provided`() = runTest {
        coEvery { repository.insertTodo(any()) } returns Unit

        viewModel.saveTodo(
            title = "Updated Todo",
            description = "Updated Description",
            isCompleted = false,
            existingId = 42L
        )
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.insertTodo(match {
                it.id == 42L &&
                it.title == "Updated Todo" &&
                it.description == "Updated Description"
            })
        }
    }

    @Test
    fun `saveTodo sets completedAt when isCompleted is true`() = runTest {
        coEvery { repository.insertTodo(any()) } returns Unit

        viewModel.saveTodo(
            title = "Completed Todo",
            description = "",
            isCompleted = true,
            existingId = null
        )
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.insertTodo(match {
                it.isCompleted && it.completedAt != null
            })
        }
    }

    @Test
    fun `saveTodo sets completedAt to null when isCompleted is false`() = runTest {
        coEvery { repository.insertTodo(any()) } returns Unit

        viewModel.saveTodo(
            title = "Incomplete Todo",
            description = "",
            isCompleted = false,
            existingId = null
        )
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.insertTodo(match {
                !it.isCompleted && it.completedAt == null
            })
        }
    }

    @Test
    fun `saveTodo emits SaveSuccess event on success`() = runTest {
        coEvery { repository.insertTodo(any()) } returns Unit

        viewModel.events.test {
            viewModel.saveTodo(
                title = "Test",
                description = "",
                isCompleted = false,
                existingId = null
            )
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(AddEditTodoEvent.SaveSuccess::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveTodo emits ShowError event on failure`() = runTest {
        val error = RuntimeException("Insert failed")
        coEvery { repository.insertTodo(any()) } throws error

        viewModel.events.test {
            viewModel.saveTodo(
                title = "Test",
                description = "",
                isCompleted = false,
                existingId = null
            )
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(AddEditTodoEvent.ShowError::class.java)
            assertThat((event as AddEditTodoEvent.ShowError).message).isEqualTo("Insert failed")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveTodo shows default error message when error message is null`() = runTest {
        val error = RuntimeException()
        coEvery { repository.insertTodo(any()) } throws error

        viewModel.events.test {
            viewModel.saveTodo(
                title = "Test",
                description = "",
                isCompleted = false,
                existingId = null
            )
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(AddEditTodoEvent.ShowError::class.java)
            assertThat((event as AddEditTodoEvent.ShowError).message).isEqualTo("An Unknown Error Occurred")
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