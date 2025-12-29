package com.lutukai.simpletodoapp.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.data.local.dao.TodoDao
import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TodoRepositoryTest {

    private lateinit var todoDao: TodoDao
    private lateinit var repository: TodoRepository

    @Before
    fun setup() {
        todoDao = mockk()
        repository = TodoRepositoryImpl(todoDao)
    }

    // ============== GET ALL TODOS TESTS ==============

    @Test
    fun `getAllTodos delegates to DAO`() = runTest {
        val todos = listOf(createTodoEntity(1, "Todo 1"), createTodoEntity(2, "Todo 2"))
        every { todoDao.getAllTodos() } returns flowOf(todos)

        repository.getAllTodos().test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result[0].title).isEqualTo("Todo 1")
            awaitComplete()
        }

        verify { todoDao.getAllTodos() }
    }

    @Test
    fun `getAllTodos emits error from DAO`() = runTest {
        val error = RuntimeException("Database error")
        every { todoDao.getAllTodos() } returns flow { throw error }

        repository.getAllTodos().test {
            val thrownError = awaitError()
            assertThat(thrownError).isInstanceOf(RuntimeException::class.java)
            assertThat(thrownError.message).isEqualTo("Database error")
        }
    }

    // ============== GET TODO BY ID TESTS ==============

    @Test
    fun `getTodoById delegates to DAO`() = runTest {
        val todo = createTodoEntity(1, "Test Todo")
        coEvery { todoDao.getTodoById(1L) } returns todo

        val result = repository.getTodoById(1L)

        assertThat(result?.title).isEqualTo("Test Todo")
        coVerify { todoDao.getTodoById(1L) }
    }

    @Test
    fun `getTodoById returns null when todo not found`() = runTest {
        coEvery { todoDao.getTodoById(999L) } returns null

        val result = repository.getTodoById(999L)

        assertThat(result).isNull()
    }

    // ============== INSERT TESTS ==============

    @Test
    fun `insertTodo delegates to DAO`() = runTest {
        val todo = createDomainTodo(title = "New Todo")
        coEvery { todoDao.insertTodo(any()) } returns Unit

        repository.insertTodo(todo)

        coVerify { todoDao.insertTodo(any()) }
    }

    @Test
    fun `insertTodoWithId delegates to DAO and returns ID`() = runTest {
        val todo = createDomainTodo(title = "New Todo")
        coEvery { todoDao.insertTodoWithId(any()) } returns 42L

        val result = repository.insertTodoWithId(todo)

        assertThat(result).isEqualTo(42L)
        coVerify { todoDao.insertTodoWithId(any()) }
    }

    @Test
    fun `insertTodo propagates error from DAO`() = runTest {
        val todo = createDomainTodo(title = "New Todo")
        val error = RuntimeException("Insert failed")
        coEvery { todoDao.insertTodo(any()) } throws error

        var thrownError: Throwable? = null
        try {
            repository.insertTodo(todo)
        } catch (e: Exception) {
            thrownError = e
        }

        assertThat(thrownError).isInstanceOf(RuntimeException::class.java)
        assertThat(thrownError?.message).isEqualTo("Insert failed")
    }

    // ============== UPDATE TESTS ==============

    @Test
    fun `updateTodo delegates to DAO`() = runTest {
        val todo = createDomainTodo(1, "Updated Todo")
        coEvery { todoDao.updateTodo(any()) } returns Unit

        repository.updateTodo(todo)

        coVerify { todoDao.updateTodo(any()) }
    }

    @Test
    fun `updateTodo propagates error from DAO`() = runTest {
        val todo = createDomainTodo(1, "Updated Todo")
        val error = RuntimeException("Update failed")
        coEvery { todoDao.updateTodo(any()) } throws error

        var thrownError: Throwable? = null
        try {
            repository.updateTodo(todo)
        } catch (e: Exception) {
            thrownError = e
        }

        assertThat(thrownError).isInstanceOf(RuntimeException::class.java)
        assertThat(thrownError?.message).isEqualTo("Update failed")
    }

    // ============== DELETE TESTS ==============

    @Test
    fun `deleteTodo delegates to DAO`() = runTest {
        val todo = createDomainTodo(1, "Todo to delete")
        coEvery { todoDao.deleteTodo(any()) } returns Unit

        repository.deleteTodo(todo)

        coVerify { todoDao.deleteTodo(any()) }
    }

    @Test
    fun `deleteTodo propagates error from DAO`() = runTest {
        val todo = createDomainTodo(1, "Todo to delete")
        val error = RuntimeException("Delete failed")
        coEvery { todoDao.deleteTodo(any()) } throws error

        var thrownError: Throwable? = null
        try {
            repository.deleteTodo(todo)
        } catch (e: Exception) {
            thrownError = e
        }

        assertThat(thrownError).isInstanceOf(RuntimeException::class.java)
        assertThat(thrownError?.message).isEqualTo("Delete failed")
    }

    @Test
    fun `deleteCompletedTodos delegates to DAO and returns count`() = runTest {
        coEvery { todoDao.deleteCompletedTodos() } returns 5

        val result = repository.deleteCompletedTodos()

        assertThat(result).isEqualTo(5)
        coVerify { todoDao.deleteCompletedTodos() }
    }

    @Test
    fun `deleteCompletedTodos propagates error from DAO`() = runTest {
        val error = RuntimeException("Delete failed")
        coEvery { todoDao.deleteCompletedTodos() } throws error

        var thrownError: Throwable? = null
        try {
            repository.deleteCompletedTodos()
        } catch (e: Exception) {
            thrownError = e
        }

        assertThat(thrownError).isInstanceOf(RuntimeException::class.java)
        assertThat(thrownError?.message).isEqualTo("Delete failed")
    }

    // ============== HELPER FUNCTIONS ==============

    private fun createTodoEntity(
        id: Long = 0,
        title: String,
        description: String = "",
        isCompleted: Boolean = false,
        completedAt: Long? = null,
        createdAt: Long = System.currentTimeMillis()
    ): TodoEntity {
        return TodoEntity(
            id = id,
            title = title,
            description = description,
            isCompleted = isCompleted,
            completedAt = completedAt,
            createdAt = createdAt
        )
    }

    private fun createDomainTodo(
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