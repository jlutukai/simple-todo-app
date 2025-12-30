package com.lutukai.simpletodoapp.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import com.lutukai.simpletodoapp.util.Result
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
    fun `invoke returns Success with todo when found`() = runTest {
        val todo = createTodo(1, "Test Todo")
        coEvery { repository.getTodoById(1L) } returns Result.Success(todo)

        val result = useCase(1L)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val data = (result as Result.Success).data
        assertThat(data?.id).isEqualTo(1L)
        assertThat(data?.title).isEqualTo("Test Todo")
        coVerify { repository.getTodoById(1L) }
    }

    @Test
    fun `invoke returns Success with null when todo not found`() = runTest {
        coEvery { repository.getTodoById(999L) } returns Result.Success(null)

        val result = useCase(999L)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isNull()
        coVerify { repository.getTodoById(999L) }
    }

    @Test
    fun `invoke returns Failure from repository`() = runTest {
        val error = RuntimeException("Database error")
        coEvery { repository.getTodoById(any()) } returns Result.Failure(error)

        val result = useCase(1L)

        assertThat(result).isInstanceOf(Result.Failure::class.java)
        assertThat((result as Result.Failure).message).isEqualTo("Database error")
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
