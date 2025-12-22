package com.lutukai.simpletodoapp.ui.tododetail

import com.lutukai.simpletodoapp.domain.models.Todo

data class TodoDetailUiState(
    val todo: Todo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class TodoDetailEvent {
    data class NavigateToEdit(val todoId: Long) : TodoDetailEvent()
    data class ShowError(val message: String) : TodoDetailEvent()
}