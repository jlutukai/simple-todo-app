package com.lutukai.simpletodoapp.ui.todolist

import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.data.repository.TodoRepository
import com.lutukai.simpletodoapp.util.schedulerprovider.SchedulerProvider
import io.reactivex.rxjava3.disposables.CompositeDisposable
import jakarta.inject.Inject

class TodoListPresenter @Inject constructor(
    private val repository: TodoRepository,
    private val schedulers: SchedulerProvider
) : TodoListContract.Presenter {
    private var view: TodoListContract.View? = null
    private val disposables = CompositeDisposable()

    override fun loadTodos() {
        view?.showLoading()
        disposables.add(
            repository.getAllTodos()
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe({todos ->
                    view?.hideLoading()
                    if(todos.isEmpty())
                        view?.showEmpty()
                    else
                        view?.showTodos(todos)
                }, {error ->
                    view?.hideLoading()
                    view?.showError(error.message?:"Unknown Error Occurred Try Again")
                })
        )
    }

    override fun addNewTodo() {
        view?.navigateToAddTodo()
    }

    override fun openTodoDetail(todo: TodoEntity) {
       view?.navigateToTodoDetail(todo.id)
    }

    override fun toggleComplete(todo: TodoEntity) {
        val newCompletedState = !todo.isCompleted
        disposables.add(
            repository.updateTodo(todo.copy(
                completedAt = if (newCompletedState) System.currentTimeMillis() else null,
                isCompleted = newCompletedState
            )).subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe({},
                    { view?.showError("Update failed") })
        )
    }

    override fun deleteTodo(todo: TodoEntity) {
        disposables.add(
            repository.deleteTodo(todo)
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(
                    { view?.showTodoDeleted(todo) },
                    { view?.showError("Delete failed") }
                )
        )
    }

    override fun undoDelete(todo: TodoEntity) {
        disposables.add(
            repository.insertTodo(todo)
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe({}, { view?.showError("Restore failed") })
        )
    }

    override fun attach(view: TodoListContract.View) {
        this.view = view
    }

    override fun detach() {
        view = null
        disposables.clear()
    }
}