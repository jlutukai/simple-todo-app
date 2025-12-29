package com.lutukai.simpletodoapp.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
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
        coEvery { repository.updateTodo(capture(todoSlot)) } returns Unit

        val result = useCase(todo)

        assertThat(result.isCompleted).isTrue()
        assertThat(result.completedAt).isNotNull()
        assertThat(todoSlot.captured.isCompleted).isTrue()
        assertThat(todoSlot.captured.completedAt).isNotNull()
        coVerify { repository.updateTodo(any()) }
    }

    @Test
    fun `invoke toggles complete todo to incomplete and clears timestamp`() = runTest {
        val todo = createTodo(id = 1, title = "Test", isCompleted = true, completedAt = 12345L)
        coEvery { repository.updateTodo(any()) } returns Unit

        val result = useCase(todo)

        assertThat(result.isCompleted).isFalse()
        assertThat(result.completedAt).isNull()
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
        coEvery { repository.updateTodo(capture(todoSlot)) } returns Unit

        val result = useCase(todo)

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.title).isEqualTo("Original Title")
        assertThat(result.description).isEqualTo("Original Description")
        assertThat(result.createdAt).isEqualTo(999L)
    }

    @Test
    fun `invoke propagates error from repository`() = runTest {
        val todo = createTodo(id = 1, title = "Test")
        coEvery { repository.updateTodo(any()) } throws RuntimeException("Update failed")

        var thrownError: Throwable? = null
        try {
            useCase(todo)
        } catch (e: Exception) {
            thrownError = e
        }

        assertThat(thrownError).isInstanceOf(RuntimeException::class.java)
        assertThat(thrownError?.message).isEqualTo("Update failed")
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
