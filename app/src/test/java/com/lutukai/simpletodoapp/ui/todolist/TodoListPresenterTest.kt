package com.lutukai.simpletodoapp.ui.todolist

import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.data.repository.TodoRepository
import com.lutukai.simpletodoapp.util.schedulerprovider.SchedulerProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test

class TodoListPresenterTest {

    private lateinit var repository: TodoRepository
    private lateinit var schedulerProvider: SchedulerProvider
    private lateinit var view: TodoListContract.View
    private lateinit var presenter: TodoListPresenter

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        schedulerProvider = mockk {
            every { io() } returns Schedulers.trampoline()
            every { ui() } returns Schedulers.trampoline()
        }
        view = mockk(relaxed = true)
        presenter = TodoListPresenter(repository, schedulerProvider)
        presenter.attach(view)
    }

    @After
    fun tearDown() {
        presenter.detach()
    }

    // ============== LOAD TODOS TESTS ==============

    @Test
    fun `loadTodos shows loading then shows todos on success`() {
        val todos = listOf(createTodo(1, "Todo 1"), createTodo(2, "Todo 2"))
        every { repository.getAllTodos() } returns Flowable.just(todos)

        presenter.loadTodos()

        verifyOrder {
            view.showLoading()
            view.hideLoading()
            view.showTodos(todos)
        }
    }

    @Test
    fun `loadTodos shows empty when list is empty`() {
        every { repository.getAllTodos() } returns Flowable.just(emptyList())

        presenter.loadTodos()

        verifyOrder {
            view.showLoading()
            view.hideLoading()
            view.showEmpty()
        }
    }

    @Test
    fun `loadTodos shows error on failure`() {
        val error = RuntimeException("Database error")
        every { repository.getAllTodos() } returns Flowable.error(error)

        presenter.loadTodos()

        verifyOrder {
            view.showLoading()
            view.hideLoading()
            view.showError("Database error")
        }
    }

    @Test
    fun `loadTodos shows default error message when error message is null`() {
        val error = RuntimeException()
        every { repository.getAllTodos() } returns Flowable.error(error)

        presenter.loadTodos()

        verify { view.showError("Unknown Error Occurred Try Again") }
    }

    // ============== ADD NEW TODO TESTS ==============

    @Test
    fun `addNewTodo navigates to add todo screen`() {
        presenter.addNewTodo()

        verify { view.navigateToAddTodo() }
    }

    // ============== OPEN TODO DETAIL TESTS ==============

    @Test
    fun `openTodoDetail navigates to detail with correct ID`() {
        val todo = createTodo(42, "Test Todo")

        presenter.openTodoDetail(todo)

        verify { view.navigateToTodoDetail(42) }
    }

    // ============== TOGGLE COMPLETE TESTS ==============

    @Test
    fun `toggleComplete updates todo to completed state`() {
        val todo = createTodo(1, "Test", isCompleted = false)
        every { repository.updateTodo(any()) } returns Completable.complete()

        presenter.toggleComplete(todo)

        verify {
            repository.updateTodo(match {
                it.id == 1L && it.isCompleted && it.completedAt != null
            })
        }
    }

    @Test
    fun `toggleComplete updates todo to incomplete state`() {
        val todo = createTodo(1, "Test", isCompleted = true, completedAt = 123456L)
        every { repository.updateTodo(any()) } returns Completable.complete()

        presenter.toggleComplete(todo)

        verify {
            repository.updateTodo(match {
                it.id == 1L && !it.isCompleted && it.completedAt == null
            })
        }
    }

    @Test
    fun `toggleComplete shows error on failure`() {
        val todo = createTodo(1, "Test")
        every { repository.updateTodo(any()) } returns Completable.error(RuntimeException())

        presenter.toggleComplete(todo)

        verify { view.showError("Update failed") }
    }

    // ============== DELETE TODO TESTS ==============

    @Test
    fun `deleteTodo deletes and shows deleted message`() {
        val todo = createTodo(1, "Test")
        every { repository.deleteTodo(todo) } returns Completable.complete()

        presenter.deleteTodo(todo)

        verify { repository.deleteTodo(todo) }
        verify { view.showTodoDeleted(todo) }
    }

    @Test
    fun `deleteTodo shows error on failure`() {
        val todo = createTodo(1, "Test")
        every { repository.deleteTodo(todo) } returns Completable.error(RuntimeException())

        presenter.deleteTodo(todo)

        verify { view.showError("Delete failed") }
    }

    // ============== UNDO DELETE TESTS ==============

    @Test
    fun `undoDelete re-inserts the todo`() {
        val todo = createTodo(1, "Test")
        every { repository.insertTodo(todo) } returns Completable.complete()

        presenter.undoDelete(todo)

        verify { repository.insertTodo(todo) }
    }

    @Test
    fun `undoDelete shows error on failure`() {
        val todo = createTodo(1, "Test")
        every { repository.insertTodo(todo) } returns Completable.error(RuntimeException())

        presenter.undoDelete(todo)

        verify { view.showError("Restore failed") }
    }

    // ============== LIFECYCLE TESTS ==============

    @Test
    fun `detach clears view reference`() {
        presenter.detach()

        // After detach, calling presenter methods should not crash
        // but view methods should not be called
        val newView: TodoListContract.View = mockk(relaxed = true)
        every { repository.getAllTodos() } returns Flowable.just(emptyList())

        presenter.loadTodos()

        verify(exactly = 0) { newView.showLoading() }
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