package com.lutukai.simpletodoapp.domain.usecases

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetAllTodosUseCaseTest {

    private lateinit var repository: TodoRepository
    private lateinit var useCase: GetAllTodosUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetAllTodosUseCase(repository)
    }

    @Test
    fun `invoke returns flow of todos from repository`() = runTest {
        val todos = listOf(createTodo(1, "Todo 1"), createTodo(2, "Todo 2"))
        every { repository.getAllTodos() } returns flowOf(todos)

        useCase().test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result[0].title).isEqualTo("Todo 1")
            assertThat(result[1].title).isEqualTo("Todo 2")
            awaitComplete()
        }

        verify { repository.getAllTodos() }
    }

    @Test
    fun `invoke returns empty list when no todos exist`() = runTest {
        every { repository.getAllTodos() } returns flowOf(emptyList())

        useCase().test {
            val result = awaitItem()
            assertThat(result).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `invoke propagates error from repository`() = runTest {
        val error = RuntimeException("Database error")
        every { repository.getAllTodos() } returns flow { throw error }

        useCase().test {
            val thrownError = awaitError()
            assertThat(thrownError).isInstanceOf(RuntimeException::class.java)
            assertThat(thrownError.message).isEqualTo("Database error")
        }
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
