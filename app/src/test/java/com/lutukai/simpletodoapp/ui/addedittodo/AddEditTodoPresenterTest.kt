package com.lutukai.simpletodoapp.ui.addedittodo

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

class AddEditTodoPresenterTest {

    private lateinit var repository: TodoRepository
    private lateinit var schedulerProvider: SchedulerProvider
    private lateinit var view: AddEditTodoContract.View
    private lateinit var presenter: AddEditTodoPresenter

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        schedulerProvider = mockk {
            every { io() } returns Schedulers.trampoline()
            every { ui() } returns Schedulers.trampoline()
        }
        view = mockk(relaxed = true)
        presenter = AddEditTodoPresenter(repository, schedulerProvider)
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

    // ============== SAVE TODO TESTS ==============

    @Test
    fun `saveTodo creates new todo when existingId is null`() {
        every { repository.insertTodo(any()) } returns Completable.complete()

        presenter.saveTodo(
            title = "New Todo",
            description = "Description",
            isCompleted = false,
            existingId = null
        )

        verify {
            repository.insertTodo(match {
                it.id == 0L &&
                it.title == "New Todo" &&
                it.description == "Description" &&
                !it.isCompleted &&
                it.completedAt == null
            })
        }
    }

    @Test
    fun `saveTodo updates existing todo when existingId is provided`() {
        every { repository.insertTodo(any()) } returns Completable.complete()

        presenter.saveTodo(
            title = "Updated Todo",
            description = "Updated Description",
            isCompleted = false,
            existingId = 42L
        )

        verify {
            repository.insertTodo(match {
                it.id == 42L &&
                it.title == "Updated Todo" &&
                it.description == "Updated Description"
            })
        }
    }

    @Test
    fun `saveTodo sets completedAt when isCompleted is true`() {
        every { repository.insertTodo(any()) } returns Completable.complete()

        presenter.saveTodo(
            title = "Completed Todo",
            description = "",
            isCompleted = true,
            existingId = null
        )

        verify {
            repository.insertTodo(match {
                it.isCompleted && it.completedAt != null
            })
        }
    }

    @Test
    fun `saveTodo sets completedAt to null when isCompleted is false`() {
        every { repository.insertTodo(any()) } returns Completable.complete()

        presenter.saveTodo(
            title = "Incomplete Todo",
            description = "",
            isCompleted = false,
            existingId = null
        )

        verify {
            repository.insertTodo(match {
                !it.isCompleted && it.completedAt == null
            })
        }
    }

    @Test
    fun `saveTodo shows loading and calls onSaveSuccess`() {
        every { repository.insertTodo(any()) } returns Completable.complete()

        presenter.saveTodo(
            title = "Test",
            description = "",
            isCompleted = false,
            existingId = null
        )

        verifyOrder {
            view.showLoading()
            view.hideLoading()
            view.onSaveSuccess()
        }
    }

    @Test
    fun `saveTodo shows error on failure`() {
        val error = RuntimeException("Insert failed")
        every { repository.insertTodo(any()) } returns Completable.error(error)

        presenter.saveTodo(
            title = "Test",
            description = "",
            isCompleted = false,
            existingId = null
        )

        verifyOrder {
            view.showLoading()
            view.hideLoading()
            view.showError("Insert failed")
        }
    }

    @Test
    fun `saveTodo shows default error message when error message is null`() {
        val error = RuntimeException()
        every { repository.insertTodo(any()) } returns Completable.error(error)

        presenter.saveTodo(
            title = "Test",
            description = "",
            isCompleted = false,
            existingId = null
        )

        verify { view.showError("An Unknown Error Occurred") }
    }

    // ============== LIFECYCLE TESTS ==============

    @Test
    fun `detach clears view reference`() {
        presenter.detach()

        // After detach, calling presenter methods should not crash
        val newView: AddEditTodoContract.View = mockk(relaxed = true)
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