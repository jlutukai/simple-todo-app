package com.lutukai.simpletodoapp.ui.todolist

import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.ui.base.BasePresenter
import com.lutukai.simpletodoapp.ui.base.BaseView

interface TodoListContract {

    interface View : BaseView {
        fun showTodos(todos: List<TodoEntity>)
        fun showEmpty()
        fun showTodoDeleted(todo: TodoEntity)
        fun navigateToAddTodo()
        fun navigateToTodoDetail(todoId: Long)
    }

    interface Presenter : BasePresenter<View> {
        fun loadTodos()
        fun addNewTodo()
        fun openTodoDetail(todo: TodoEntity)
        fun toggleComplete(todo: TodoEntity)
        fun deleteTodo(todo: TodoEntity)
        fun undoDelete(todo: TodoEntity)
    }
}