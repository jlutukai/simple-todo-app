package com.lutukai.simpletodoapp.data.repository

import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.data.local.dao.TodoDao
import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.util.schedulerprovider.SchedulerProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class TodoRepositoryTest {

    private lateinit var todoDao: TodoDao
    private lateinit var schedulerProvider: SchedulerProvider
    private lateinit var repository: TodoRepository

    @Before
    fun setup() {
        todoDao = mockk()
        schedulerProvider = mockk {
            every { io() } returns Schedulers.trampoline()
            every { ui() } returns Schedulers.trampoline()
            every { computation() } returns Schedulers.trampoline()
        }
        repository = TodoRepository(todoDao, schedulerProvider)
    }

    // ============== GET ALL TODOS TESTS ==============

    @Test
    fun `getAllTodos delegates to DAO`() {
        val todos = listOf(createTodo(1, "Todo 1"), createTodo(2, "Todo 2"))
        every { todoDao.getAllTodos() } returns Flowable.just(todos)

        val result = repository.getAllTodos().blockingFirst()

        assertThat(result).hasSize(2)
        assertThat(result[0].title).isEqualTo("Todo 1")
        verify { todoDao.getAllTodos() }
    }

    @Test
    fun `getAllTodos subscribes on IO scheduler`() {
        every { todoDao.getAllTodos() } returns Flowable.just(emptyList())

        repository.getAllTodos().blockingFirst()

        verify { schedulerProvider.io() }
    }

    @Test
    fun `getAllTodos emits error from DAO`() {
        val error = RuntimeException("Database error")
        every { todoDao.getAllTodos() } returns Flowable.error(error)

        val testObserver = repository.getAllTodos().test()

        testObserver.assertError { it is RuntimeException || it.cause is RuntimeException }
    }

    // ============== GET TODO BY ID TESTS ==============

    @Test
    fun `getTodoById delegates to DAO`() {
        val todo = createTodo(1, "Test Todo")
        every { todoDao.getTodoById(1L) } returns Maybe.just(todo)

        val result = repository.getTodoById(1L).blockingGet()

        assertThat(result?.title).isEqualTo("Test Todo")
        verify { todoDao.getTodoById(1L) }
    }

    @Test
    fun `getTodoById returns empty when todo not found`() {
        every { todoDao.getTodoById(999L) } returns Maybe.empty()

        val testObserver = repository.getTodoById(999L).test()

        testObserver.assertComplete()
        testObserver.assertNoValues()
    }

    @Test
    fun `getTodoByIdOrError delegates to DAO`() {
        val todo = createTodo(1, "Test Todo")
        every { todoDao.getTodoByIdOrError(1L) } returns Single.just(todo)

        val result = repository.getTodoByIdOrError(1L).blockingGet()

        assertThat(result.title).isEqualTo("Test Todo")
        verify { todoDao.getTodoByIdOrError(1L) }
    }

    @Test
    fun `getTodoByIdOrError propagates error when todo not found`() {
        val error = RuntimeException("Todo not found")
        every { todoDao.getTodoByIdOrError(999L) } returns Single.error(error)

        val testObserver = repository.getTodoByIdOrError(999L).test()

        testObserver.assertError { it is RuntimeException || it.cause is RuntimeException }
    }

    // ============== INSERT TESTS ==============

    @Test
    fun `insertTodo delegates to DAO`() {
        val todo = createTodo(title = "New Todo")
        every { todoDao.insertTodo(todo) } returns Completable.complete()

        repository.insertTodo(todo).blockingAwait()

        verify { todoDao.insertTodo(todo) }
    }

    @Test
    fun `insertTodo subscribes on IO scheduler`() {
        val todo = createTodo(title = "New Todo")
        every { todoDao.insertTodo(todo) } returns Completable.complete()

        repository.insertTodo(todo).blockingAwait()

        verify { schedulerProvider.io() }
    }

    @Test
    fun `insertTodoWithId delegates to DAO and returns ID`() {
        val todo = createTodo(title = "New Todo")
        every { todoDao.insertTodoWithId(todo) } returns Single.just(42L)

        val result = repository.insertTodoWithId(todo).blockingGet()

        assertThat(result).isEqualTo(42L)
        verify { todoDao.insertTodoWithId(todo) }
    }

    @Test
    fun `insertTodo propagates error from DAO`() {
        val todo = createTodo(title = "New Todo")
        val error = RuntimeException("Insert failed")
        every { todoDao.insertTodo(todo) } returns Completable.error(error)

        val testObserver = repository.insertTodo(todo).test()

        testObserver.assertError { it is RuntimeException || it.cause is RuntimeException }
    }

    // ============== UPDATE TESTS ==============

    @Test
    fun `updateTodo delegates to DAO`() {
        val todo = createTodo(1, "Updated Todo")
        every { todoDao.updateTodo(todo) } returns Completable.complete()

        repository.updateTodo(todo).blockingAwait()

        verify { todoDao.updateTodo(todo) }
    }

    @Test
    fun `updateTodo subscribes on IO scheduler`() {
        val todo = createTodo(1, "Updated Todo")
        every { todoDao.updateTodo(todo) } returns Completable.complete()

        repository.updateTodo(todo).blockingAwait()

        verify { schedulerProvider.io() }
    }

    @Test
    fun `updateTodo propagates error from DAO`() {
        val todo = createTodo(1, "Updated Todo")
        val error = RuntimeException("Update failed")
        every { todoDao.updateTodo(todo) } returns Completable.error(error)

        val testObserver = repository.updateTodo(todo).test()

        testObserver.assertError { it is RuntimeException || it.cause is RuntimeException }
    }

    // ============== DELETE TESTS ==============

    @Test
    fun `deleteTodo delegates to DAO`() {
        val todo = createTodo(1, "Todo to delete")
        every { todoDao.deleteTodo(todo) } returns Completable.complete()

        repository.deleteTodo(todo).blockingAwait()

        verify { todoDao.deleteTodo(todo) }
    }

    @Test
    fun `deleteTodo subscribes on IO scheduler`() {
        val todo = createTodo(1, "Todo to delete")
        every { todoDao.deleteTodo(todo) } returns Completable.complete()

        repository.deleteTodo(todo).blockingAwait()

        verify { schedulerProvider.io() }
    }

    @Test
    fun `deleteTodo propagates error from DAO`() {
        val todo = createTodo(1, "Todo to delete")
        val error = RuntimeException("Delete failed")
        every { todoDao.deleteTodo(todo) } returns Completable.error(error)

        val testObserver = repository.deleteTodo(todo).test()

        testObserver.assertError { it is RuntimeException || it.cause is RuntimeException }
    }

    @Test
    fun `deleteCompletedTodos delegates to DAO and returns count`() {
        every { todoDao.deleteCompletedTodos() } returns Single.just(5)

        val result = repository.deleteCompletedTodos().blockingGet()

        assertThat(result).isEqualTo(5)
        verify { todoDao.deleteCompletedTodos() }
    }

    @Test
    fun `deleteCompletedTodos subscribes on IO scheduler`() {
        every { todoDao.deleteCompletedTodos() } returns Single.just(0)

        repository.deleteCompletedTodos().blockingGet()

        verify { schedulerProvider.io() }
    }

    @Test
    fun `deleteCompletedTodos propagates error from DAO`() {
        val error = RuntimeException("Delete failed")
        every { todoDao.deleteCompletedTodos() } returns Single.error(error)

        val testObserver = repository.deleteCompletedTodos().test()

        testObserver.assertError { it is RuntimeException || it.cause is RuntimeException }
    }

    // ============== HELPER FUNCTIONS ==============

    private fun createTodo(
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
}