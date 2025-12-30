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

class InsertTodoUseCaseTest {

    private lateinit var repository: TodoRepository
    private lateinit var useCase: InsertTodoUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = InsertTodoUseCase(repository)
    }

    @Test
    fun `invoke calls repository insertTodo`() = runTest {
        val todo = createTodo(title = "New Todo")
        coEvery { repository.insertTodo(any()) } returns Result.Success(Unit)

        val result = useCase(todo)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerify { repository.insertTodo(todo) }
    }

    @Test
    fun `invoke returns Failure from repository`() = runTest {
        val todo = createTodo(title = "New Todo")
        val error = RuntimeException("Insert failed")
        coEvery { repository.insertTodo(any()) } returns Result.Failure(error)

        val result = useCase(todo)

        assertThat(result).isInstanceOf(Result.Failure::class.java)
        assertThat((result as Result.Failure).message).isEqualTo("Insert failed")
    }

    private fun createTodo(
        id: Long? = null,
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
