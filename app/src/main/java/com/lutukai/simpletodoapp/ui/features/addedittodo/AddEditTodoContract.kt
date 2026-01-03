package com.lutukai.simpletodoapp.ui.features.addedittodo

import androidx.compose.runtime.Stable
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.ui.mvi.SideEffect
import com.lutukai.simpletodoapp.ui.mvi.UiIntent
import com.lutukai.simpletodoapp.ui.mvi.UiState

@Stable
data class AddEditTodoState(
    val todo: Todo? = null,
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false
) : UiState {
    val isSaveEnabled: Boolean
        get() = title.isNotBlank() && !isLoading
}

sealed interface AddEditTodoIntent : UiIntent {
    data class LoadTodo(val todoId: Long) : AddEditTodoIntent
    data class UpdateTitle(val title: String) : AddEditTodoIntent
    data class UpdateDescription(val description: String) : AddEditTodoIntent
    data class UpdateCompleted(val isCompleted: Boolean) : AddEditTodoIntent
    data object SaveTodo : AddEditTodoIntent
    data object Cancel : AddEditTodoIntent
}

sealed interface AddEditTodoSideEffect : SideEffect {
    data object SaveSuccess : AddEditTodoSideEffect
    data class ShowError(val message: String) : AddEditTodoSideEffect
    data object Dismiss : AddEditTodoSideEffect
}
