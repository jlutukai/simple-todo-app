package com.lutukai.simpletodoapp.ui.tododetail

import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.ui.base.BasePresenter
import com.lutukai.simpletodoapp.ui.base.BaseView

interface TodoDetailContract {

    interface View : BaseView {
        fun showTodo(todo: TodoEntity)
        fun navigateToEdit(todoId: Long)
        fun closeDialog()
    }

    interface Presenter : BasePresenter<View> {
        fun loadTodo(todoId: Long)
        fun toggleComplete(isCompleted: Boolean)
        fun onEditClicked()
    }
}
