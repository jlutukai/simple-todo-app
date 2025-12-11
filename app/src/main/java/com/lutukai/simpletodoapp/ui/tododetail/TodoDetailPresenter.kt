package com.lutukai.simpletodoapp.ui.tododetail

import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.data.repository.TodoRepository
import com.lutukai.simpletodoapp.util.schedulerprovider.SchedulerProvider
import io.reactivex.rxjava3.disposables.CompositeDisposable
import jakarta.inject.Inject

class TodoDetailPresenter @Inject constructor(
    private val repository: TodoRepository,
    private val schedulers: SchedulerProvider
) : TodoDetailContract.Presenter {

    private var view: TodoDetailContract.View? = null
    private val disposables = CompositeDisposable()
    private var currentTodo: TodoEntity? = null

    override fun loadTodo(todoId: Long) {
        view?.showLoading()
        disposables.add(
            repository.getTodoByIdOrError(todoId)
                .observeOn(schedulers.ui())
                .subscribe({ todo ->
                    view?.hideLoading()
                    currentTodo = todo
                    view?.showTodo(todo)
                }, { error ->
                    view?.hideLoading()
                    view?.showError(error.message ?: "An Unknown Error Occurred")
                })
        )
    }

    override fun toggleComplete(isCompleted: Boolean) {
        val todo = currentTodo ?: return

        val updatedTodo = todo.copy(
            isCompleted = isCompleted,
            completedAt = if (isCompleted) System.currentTimeMillis() else null
        )

        disposables.add(
            repository.updateTodo(updatedTodo)
                .observeOn(schedulers.ui())
                .subscribe({
                    currentTodo = updatedTodo
                    view?.showTodo(updatedTodo)
                }, { error ->
                    view?.showError(error.message ?: "Failed to update task")
                    // Revert switch state by showing current todo
                    view?.showTodo(todo)
                })
        )
    }

    override fun onEditClicked() {
        currentTodo?.let { todo ->
            view?.navigateToEdit(todo.id)
        }
    }

    override fun attach(view: TodoDetailContract.View) {
        this.view = view
    }

    override fun detach() {
        view = null
        disposables.clear()
    }
}
