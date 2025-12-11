package com.lutukai.simpletodoapp.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.data.local.database.AppDataBase
import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodoDaoTest {

    private lateinit var database: AppDataBase
    private lateinit var todoDao: TodoDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDataBase::class.java
        ).allowMainThreadQueries().build()

        todoDao = database.todoDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // ============== INSERT TESTS ==============

    @Test
    fun insertTodo_completesSuccessfully() {
        val todo = createTodo(title = "Test Todo")

        todoDao.insertTodo(todo)
            .test()
            .assertComplete()
            .assertNoErrors()
    }

    @Test
    fun insertTodoWithId_returnsInsertedRowId() {
        val todo = createTodo(title = "Test Todo")

        val testObserver = todoDao.insertTodoWithId(todo).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        val insertedId = testObserver.values()[0]
        assertThat(insertedId).isGreaterThan(0)
    }

    @Test
    fun insertMultipleTodos_returnsIncrementingIds() {
        val todo1 = createTodo(title = "Todo 1")
        val todo2 = createTodo(title = "Todo 2")

        val id1 = todoDao.insertTodoWithId(todo1).blockingGet()
        val id2 = todoDao.insertTodoWithId(todo2).blockingGet()

        assertThat(id2).isGreaterThan(id1)
    }

    // ============== GET ALL TODOS TESTS ==============

    @Test
    fun getAllTodos_emptyDatabase_returnsEmptyList() {
        todoDao.getAllTodos()
            .test()
            .assertValue { it.isEmpty() }
    }

    @Test
    fun getAllTodos_withTodos_returnsAllTodos() {
        val todo1 = createTodo(title = "Todo 1")
        val todo2 = createTodo(title = "Todo 2")
        todoDao.insertTodo(todo1).blockingAwait()
        todoDao.insertTodo(todo2).blockingAwait()

        todoDao.getAllTodos()
            .test()
            .assertValue { todos ->
                todos.size == 2 &&
                todos.any { it.title == "Todo 1" } &&
                todos.any { it.title == "Todo 2" }
            }
    }

    @Test
    fun getAllTodos_orderedByCreatedAtDescending() {
        val olderTodo = createTodo(title = "Older", createdAt = 1000L)
        val newerTodo = createTodo(title = "Newer", createdAt = 2000L)
        todoDao.insertTodo(olderTodo).blockingAwait()
        todoDao.insertTodo(newerTodo).blockingAwait()

        todoDao.getAllTodos()
            .test()
            .assertValue { todos ->
                todos[0].title == "Newer" && todos[1].title == "Older"
            }
    }

    @Test
    fun getAllTodos_emitsNewListOnInsert() {
        val testObserver = todoDao.getAllTodos().test()

        // Initial emission - empty list
        testObserver.assertValueAt(0) { it.isEmpty() }

        // Insert a todo
        val todo = createTodo(title = "New Todo")
        todoDao.insertTodo(todo).blockingAwait()

        // Should emit new list with the todo
        testObserver.assertValueAt(1) { it.size == 1 && it[0].title == "New Todo" }
    }

    // ============== GET TODO BY ID TESTS ==============

    @Test
    fun getTodoById_existingTodo_returnsTodo() {
        val todo = createTodo(title = "Test Todo")
        val insertedId = todoDao.insertTodoWithId(todo).blockingGet()

        todoDao.getTodoById(insertedId)
            .test()
            .assertValue { it.title == "Test Todo" }
            .assertComplete()
    }

    @Test
    fun getTodoById_nonExistingTodo_completesEmpty() {
        todoDao.getTodoById(999L)
            .test()
            .assertComplete()
            .assertNoValues()
    }

    @Test
    fun getTodoByIdOrError_existingTodo_returnsTodo() {
        val todo = createTodo(title = "Test Todo")
        val insertedId = todoDao.insertTodoWithId(todo).blockingGet()

        todoDao.getTodoByIdOrError(insertedId)
            .test()
            .assertValue { it.title == "Test Todo" }
            .assertComplete()
    }

    @Test
    fun getTodoByIdOrError_nonExistingTodo_throwsError() {
        todoDao.getTodoByIdOrError(999L)
            .test()
            .assertError { true }
    }

    // ============== UPDATE TESTS ==============

    @Test
    fun updateTodo_completesSuccessfully() {
        val todo = createTodo(title = "Original Title")
        val insertedId = todoDao.insertTodoWithId(todo).blockingGet()

        val updatedTodo = todo.copy(id = insertedId, title = "Updated Title")
        todoDao.updateTodo(updatedTodo)
            .test()
            .assertComplete()
            .assertNoErrors()
    }

    @Test
    fun updateTodo_changesPersistedInDatabase() {
        val todo = createTodo(title = "Original Title")
        val insertedId = todoDao.insertTodoWithId(todo).blockingGet()

        val updatedTodo = todo.copy(id = insertedId, title = "Updated Title", isCompleted = true)
        todoDao.updateTodo(updatedTodo).blockingAwait()

        todoDao.getTodoById(insertedId)
            .test()
            .assertValue { it.title == "Updated Title" && it.isCompleted }
    }

    @Test
    fun updateTodo_triggersFlowableEmission() {
        val todo = createTodo(title = "Original")
        val insertedId = todoDao.insertTodoWithId(todo).blockingGet()

        val testObserver = todoDao.getAllTodos().test()

        // Initial state
        testObserver.assertValueAt(0) { it[0].title == "Original" }

        // Update
        val updatedTodo = todo.copy(id = insertedId, title = "Updated")
        todoDao.updateTodo(updatedTodo).blockingAwait()

        // Should emit updated list
        testObserver.assertValueAt(1) { it[0].title == "Updated" }
    }

    // ============== DELETE TESTS ==============

    @Test
    fun deleteTodo_completesSuccessfully() {
        val todo = createTodo(title = "Test Todo")
        val insertedId = todoDao.insertTodoWithId(todo).blockingGet()
        val insertedTodo = todoDao.getTodoById(insertedId).blockingGet()!!

        todoDao.deleteTodo(insertedTodo)
            .test()
            .assertComplete()
            .assertNoErrors()
    }

    @Test
    fun deleteTodo_removesTodoFromDatabase() {
        val todo = createTodo(title = "Test Todo")
        val insertedId = todoDao.insertTodoWithId(todo).blockingGet()
        val insertedTodo = todoDao.getTodoById(insertedId).blockingGet()!!

        todoDao.deleteTodo(insertedTodo).blockingAwait()

        todoDao.getTodoById(insertedId)
            .test()
            .assertComplete()
            .assertNoValues()
    }

    @Test
    fun deleteCompletedTodos_returnsDeletedCount() {
        val completedTodo1 = createTodo(title = "Completed 1", isCompleted = true)
        val completedTodo2 = createTodo(title = "Completed 2", isCompleted = true)
        val incompleteTodo = createTodo(title = "Incomplete", isCompleted = false)

        todoDao.insertTodo(completedTodo1).blockingAwait()
        todoDao.insertTodo(completedTodo2).blockingAwait()
        todoDao.insertTodo(incompleteTodo).blockingAwait()

        todoDao.deleteCompletedTodos()
            .test()
            .assertValue(2)
            .assertComplete()
    }

    @Test
    fun deleteCompletedTodos_onlyRemovesCompletedTodos() {
        val completedTodo = createTodo(title = "Completed", isCompleted = true)
        val incompleteTodo = createTodo(title = "Incomplete", isCompleted = false)

        todoDao.insertTodo(completedTodo).blockingAwait()
        todoDao.insertTodo(incompleteTodo).blockingAwait()

        todoDao.deleteCompletedTodos().blockingGet()

        todoDao.getAllTodos()
            .test()
            .assertValue { todos ->
                todos.size == 1 && todos[0].title == "Incomplete"
            }
    }

    @Test
    fun deleteCompletedTodos_noCompletedTodos_returnsZero() {
        val incompleteTodo = createTodo(title = "Incomplete", isCompleted = false)
        todoDao.insertTodo(incompleteTodo).blockingAwait()

        todoDao.deleteCompletedTodos()
            .test()
            .assertValue(0)
            .assertComplete()
    }

    // ============== HELPER FUNCTIONS ==============

    private fun createTodo(
        title: String,
        description: String = "",
        isCompleted: Boolean = false,
        completedAt: Long? = null,
        createdAt: Long = System.currentTimeMillis()
    ): TodoEntity {
        return TodoEntity(
            title = title,
            description = description,
            isCompleted = isCompleted,
            completedAt = completedAt,
            createdAt = createdAt
        )
    }
}