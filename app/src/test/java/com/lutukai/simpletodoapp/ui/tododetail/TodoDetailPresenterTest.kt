package com.lutukai.simpletodoapp.ui.tododetail

import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.data.repository.TodoRepository
import com.lutukai.simpletodoapp.util.schedulerprovider.SchedulerProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test

class TodoDetailPresenterTest {

    private lateinit var repository: TodoRepository
    private lateinit var schedulerProvider: SchedulerProvider
    private lateinit var view: TodoDetailContract.View
    private lateinit var presenter: TodoDetailPresenter

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        schedulerProvider = mockk {
            every { io() } returns Schedulers.trampoline()
            every { ui() } returns Schedulers.trampoline()
        }
        view = mockk(relaxed = true)
        presenter = TodoDetailPresenter(repository, schedulerProvider)
        presenter.attach(view)
    }

    @After
    fun tearDown() {
        presenter.detach()
    }

    // ============== LOAD TODO TESTS ==============

    @Test
    fun `loadTodo shows loading then displays todo on success`() {
        val todo = createTodo(1, "Test Todo")
        every { repository.getTodoByIdOrError(1L) } returns Single.just(todo)

        presenter.loadTodo(1L)

        verifyOrder {
            view.showLoading()
            view.hideLoading()
            view.showTodo(todo)
        }
    }

    @Test
    fun `loadTodo shows error on failure`() {
        val error = RuntimeException("Todo not found")
        every { repository.getTodoByIdOrError(1L) } returns Single.error(error)

        presenter.loadTodo(1L)

        verifyOrder {
            view.showLoading()
            view.hideLoading()
            view.showError("Todo not found")
        }
    }

    @Test
    fun `loadTodo shows default error message when error message is null`() {
        val error = RuntimeException()
        every { repository.getTodoByIdOrError(1L) } returns Single.error(error)

        presenter.loadTodo(1L)

        verify { view.showError("An Unknown Error Occurred") }
    }

    // ============== TOGGLE COMPLETE TESTS ==============

    @Test
    fun `toggleComplete updates todo to completed state`() {
        val todo = createTodo(1, "Test", isCompleted = false)
        every { repository.getTodoByIdOrError(1L) } returns Single.just(todo)
        every { repository.updateTodo(any()) } returns Completable.complete()

        // First load the todo to set currentTodo
        presenter.loadTodo(1L)

        // Then toggle complete
        presenter.toggleComplete(true)

        verify {
            repository.updateTodo(match {
                it.id == 1L && it.isCompleted && it.completedAt != null
            })
        }
    }

    @Test
    fun `toggleComplete updates todo to incomplete state`() {
        val todo = createTodo(1, "Test", isCompleted = true, completedAt = 123456L)
        every { repository.getTodoByIdOrError(1L) } returns Single.just(todo)
        every { repository.updateTodo(any()) } returns Completable.complete()

        // First load the todo to set currentTodo
        presenter.loadTodo(1L)

        // Then toggle complete
        presenter.toggleComplete(false)

        verify {
            repository.updateTodo(match {
                it.id == 1L && !it.isCompleted && it.completedAt == null
            })
        }
    }

    @Test
    fun `toggleComplete shows updated todo on success`() {
        val todo = createTodo(1, "Test", isCompleted = false)
        every { repository.getTodoByIdOrError(1L) } returns Single.just(todo)
        every { repository.updateTodo(any()) } returns Completable.complete()

        presenter.loadTodo(1L)
        presenter.toggleComplete(true)

        // Should show the updated todo after successful update
        verify(exactly = 2) { view.showTodo(any()) }
    }

    @Test
    fun `toggleComplete reverts UI on failure`() {
        val todo = createTodo(1, "Test", isCompleted = false)
        every { repository.getTodoByIdOrError(1L) } returns Single.just(todo)
        every { repository.updateTodo(any()) } returns Completable.error(RuntimeException("Update failed"))

        presenter.loadTodo(1L)
        presenter.toggleComplete(true)

        // Should show error and revert to original todo state
        verify { view.showError("Update failed") }
        // showTodo called twice: once after load, once to revert
        verify(exactly = 2) { view.showTodo(todo) }
    }

    @Test
    fun `toggleComplete shows default error message when error message is null`() {
        val todo = createTodo(1, "Test", isCompleted = false)
        every { repository.getTodoByIdOrError(1L) } returns Single.just(todo)
        every { repository.updateTodo(any()) } returns Completable.error(RuntimeException())

        presenter.loadTodo(1L)
        presenter.toggleComplete(true)

        verify { view.showError("Failed to update task") }
    }

    @Test
    fun `toggleComplete does nothing when currentTodo is null`() {
        // Don't load any todo, so currentTodo remains null
        presenter.toggleComplete(true)

        verify(exactly = 0) { repository.updateTodo(any()) }
    }

    // ============== ON EDIT CLICKED TESTS ==============

    @Test
    fun `onEditClicked navigates to edit with correct ID`() {
        val todo = createTodo(42, "Test Todo")
        every { repository.getTodoByIdOrError(42L) } returns Single.just(todo)

        presenter.loadTodo(42L)
        presenter.onEditClicked()

        verify { view.navigateToEdit(42L) }
    }

    @Test
    fun `onEditClicked does nothing when currentTodo is null`() {
        // Don't load any todo, so currentTodo remains null
        presenter.onEditClicked()

        verify(exactly = 0) { view.navigateToEdit(any()) }
    }

    // ============== LIFECYCLE TESTS ==============

    @Test
    fun `detach clears view reference`() {
        presenter.detach()

        // After detach, calling presenter methods should not crash
        val newView: TodoDetailContract.View = mockk(relaxed = true)
        every { repository.getTodoByIdOrError(1L) } returns Single.just(createTodo(1, "Test"))

        presenter.loadTodo(1L)

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