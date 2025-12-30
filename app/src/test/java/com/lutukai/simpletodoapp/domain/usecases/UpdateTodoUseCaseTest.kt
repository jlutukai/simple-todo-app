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

class UpdateTodoUseCaseTest {

    private lateinit var repository: TodoRepository
    private lateinit var useCase: UpdateTodoUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = UpdateTodoUseCase(repository)
    }

    @Test
    fun `invoke calls repository updateTodo`() = runTest {
        val todo = createTodo(1, "Updated Todo")
        coEvery { repository.updateTodo(any()) } returns Result.Success(Unit)

        val result = useCase(todo)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerify { repository.updateTodo(todo) }
    }

    @Test
    fun `invoke returns Failure from repository`() = runTest {
        val todo = createTodo(1, "Updated Todo")
        val error = RuntimeException("Update failed")
        coEvery { repository.updateTodo(any()) } returns Result.Failure(error)

        val result = useCase(todo)

        assertThat(result).isInstanceOf(Result.Failure::class.java)
        assertThat((result as Result.Failure).message).isEqualTo("Update failed")
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
