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
        coEvery { repository.insertTodo(any()) } returns Unit

        useCase(todo)

        coVerify { repository.insertTodo(todo) }
    }

    @Test
    fun `invoke propagates error from repository`() = runTest {
        val todo = createTodo(title = "New Todo")
        val error = RuntimeException("Insert failed")
        coEvery { repository.insertTodo(any()) } throws error

        var thrownError: Throwable? = null
        try {
            useCase(todo)
        } catch (e: Exception) {
            thrownError = e
        }

        assertThat(thrownError).isInstanceOf(RuntimeException::class.java)
        assertThat(thrownError?.message).isEqualTo("Insert failed")
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
