package com.lutukai.simpletodoapp.ui.addedittodo

import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.data.repository.TodoRepository
import com.lutukai.simpletodoapp.util.schedulerprovider.SchedulerProvider
import io.reactivex.rxjava3.disposables.CompositeDisposable
import jakarta.inject.Inject

class AddEditTodoPresenter @Inject constructor(
    private val repository: TodoRepository,
    private val schedulers: SchedulerProvider
) : AddEditTodoContract.Presenter {
    private var view: AddEditTodoContract.View? = null
    private val disposables = CompositeDisposable()
    override fun loadTodo(todoId: Long) {
        view?.showLoading()
        disposables.add(
            repository.getTodoByIdOrError(todoId)
                .observeOn(schedulers.ui())
                .subscribe({ data ->
                    view?.hideLoading()
                    view?.showTodo(data)
                }, { error ->
                    view?.hideLoading()
                    view?.showError(error.message ?: "An Unknown Error Occurred")
                })
        )
    }

    override fun saveTodo(
        title: String,
        description: String,
        isCompleted: Boolean,
        existingId: Long?
    ) {
        view?.showLoading()
        disposables.add(
            repository.insertTodo(
                TodoEntity(
                    id = existingId?:0,
                    title = title,
                    description = description,
                    isCompleted = isCompleted,
                    completedAt = if(isCompleted) System.currentTimeMillis() else null
                )
            ).observeOn(schedulers.ui())
                .subscribe({
                    view?.hideLoading()
                    view?.onSaveSuccess()
                }, { error ->
                    view?.hideLoading()
                    view?.showError(error.message ?: "An Unknown Error Occurred")
                })
        )
    }

    override fun attach(view: AddEditTodoContract.View) {
        this.view = view
    }

    override fun detach() {
      view = null
        disposables.clear()
    }
}