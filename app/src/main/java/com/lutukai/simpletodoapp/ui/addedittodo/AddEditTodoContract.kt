package com.lutukai.simpletodoapp.ui.addedittodo

import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.ui.base.BasePresenter
import com.lutukai.simpletodoapp.ui.base.BaseView

interface AddEditTodoContract {

    interface View : BaseView {
        fun showTodo(todo: TodoEntity)
        fun onSaveSuccess()
    }

    interface Presenter : BasePresenter<View> {
        fun loadTodo(todoId: Long)
        fun saveTodo(title: String, description: String, isCompleted: Boolean, existingId: Long?)
    }
}