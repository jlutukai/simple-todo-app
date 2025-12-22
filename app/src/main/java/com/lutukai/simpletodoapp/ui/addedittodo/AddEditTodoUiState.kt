package com.lutukai.simpletodoapp.ui.addedittodo

import com.lutukai.simpletodoapp.domain.models.Todo

data class AddEditTodoUiState(
    val todo: Todo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class AddEditTodoEvent {
    data object SaveSuccess : AddEditTodoEvent()
    data class ShowError(val message: String) : AddEditTodoEvent()
}