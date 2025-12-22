package com.lutukai.simpletodoapp.ui.todolist

import com.lutukai.simpletodoapp.domain.models.Todo

data class TodoListUiState(
    val todos: List<Todo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class TodoListEvent {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val action: (() -> Unit)? = null
    ) : TodoListEvent()
    data class NavigateToDetail(val todoId: Long) : TodoListEvent()
    data object NavigateToAddTodo : TodoListEvent()
}