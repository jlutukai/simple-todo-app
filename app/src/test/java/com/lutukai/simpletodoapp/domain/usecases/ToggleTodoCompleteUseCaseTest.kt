package com.lutukai.simpletodoapp.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import com.lutukai.simpletodoapp.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ToggleTodoCompleteUseCaseTest {

    private lateinit var repository: TodoRepository
    private lateinit var useCase: ToggleTodoCompleteUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = ToggleTodoCompleteUseCase(repository)
    }

    @Test
    fun `invoke toggles incomplete todo to complete with timestamp`() = runTest {
        val todo = createTodo(id = 1, title = "Test", isCompleted = false)
        val todoSlot = slot<Todo>()
        coEvery { repository.updateTodo(capture(todoSlot)) } returns Result.Success(Unit)

        val result = useCase(todo)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val data = (result as Result.Success).data
        assertThat(data.isCompleted).isTrue()
        assertThat(data.completedAt).isNotNull()
        assertThat(todoSlot.captured.isCompleted).isTrue()
        assertThat(todoSlot.captured.completedAt).isNotNull()
        coVerify { repository.updateTodo(any()) }
    }

    @Test
    fun `invoke toggles complete todo to incomplete and clears timestamp`() = runTest {
        val todo = createTodo(id = 1, title = "Test", isCompleted = true, completedAt = 12345L)
        coEvery { repository.updateTodo(any()) } returns Result.Success(Unit)

        val result = useCase(todo)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val data = (result as Result.Success).data
        assertThat(data.isCompleted).isFalse()
        assertThat(data.completedAt).isNull()
    }

    @Test
    fun `invoke preserves other todo properties`() = runTest {
        val todo = Todo(
            id = 1,
            title = "Original Title",
            description = "Original Description",
            isCompleted = false,
            completedAt = null,
            createdAt = 999L
        )
        val todoSlot = slot<Todo>()
        coEvery { repository.updateTodo(capture(todoSlot)) } returns Result.Success(Unit)

        val result = useCase(todo)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val data = (result as Result.Success).data
        assertThat(data.id).isEqualTo(1L)
        assertThat(data.title).isEqualTo("Original Title")
        assertThat(data.description).isEqualTo("Original Description")
        assertThat(data.createdAt).isEqualTo(999L)
    }

    @Test
    fun `invoke returns Failure from repository`() = runTest {
        val todo = createTodo(id = 1, title = "Test")
        coEvery { repository.updateTodo(any()) } returns Result.Failure(RuntimeException("Update failed"))

        val result = useCase(todo)

        assertThat(result).isInstanceOf(Result.Failure::class.java)
        assertThat((result as Result.Failure).message).isEqualTo("Update failed")
    }

    private fun createTodo(
        id: Long,
        title: String,
        isCompleted: Boolean = false,
        completedAt: Long? = null
    ) = Todo(
        id = id,
        title = title,
        description = "",
        isCompleted = isCompleted,
        completedAt = completedAt,
        createdAt = System.currentTimeMillis()
    )
}
