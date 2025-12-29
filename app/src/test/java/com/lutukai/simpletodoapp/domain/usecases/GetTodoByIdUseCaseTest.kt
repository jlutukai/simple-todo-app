package com.lutukai.simpletodoapp.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetTodoByIdUseCaseTest {

    private lateinit var repository: TodoRepository
    private lateinit var useCase: GetTodoByIdUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetTodoByIdUseCase(repository)
    }

    @Test
    fun `invoke returns todo when found`() = runTest {
        val todo = createTodo(1, "Test Todo")
        coEvery { repository.getTodoById(1L) } returns todo

        val result = useCase(1L)

        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(1L)
        assertThat(result?.title).isEqualTo("Test Todo")
        coVerify { repository.getTodoById(1L) }
    }

    @Test
    fun `invoke returns null when todo not found`() = runTest {
        coEvery { repository.getTodoById(999L) } returns null

        val result = useCase(999L)

        assertThat(result).isNull()
        coVerify { repository.getTodoById(999L) }
    }

    @Test
    fun `invoke propagates error from repository`() = runTest {
        val error = RuntimeException("Database error")
        coEvery { repository.getTodoById(any()) } throws error

        var thrownError: Throwable? = null
        try {
            useCase(1L)
        } catch (e: Exception) {
            thrownError = e
        }

        assertThat(thrownError).isInstanceOf(RuntimeException::class.java)
        assertThat(thrownError?.message).isEqualTo("Database error")
    }

    private fun createTodo(
        id: Long,
        title: String,
        isCompleted: Boolean = false
    ) = Todo(
        id = id,
        title = title,
        description = "",
        isCompleted = isCompleted,
        completedAt = null,
        createdAt = System.currentTimeMillis()
    )
}
