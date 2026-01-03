package com.lutukai.simpletodoapp.ui.features.tododetail

import androidx.compose.runtime.Stable
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.ui.mvi.SideEffect
import com.lutukai.simpletodoapp.ui.mvi.UiIntent
import com.lutukai.simpletodoapp.ui.mvi.UiState

@Stable
data class TodoDetailState(
    val todo: Todo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed interface TodoDetailIntent : UiIntent {
    data class LoadTodo(val todoId: Long) : TodoDetailIntent
    data class ToggleComplete(val isCompleted: Boolean) : TodoDetailIntent
    data object EditClicked : TodoDetailIntent
    data object Dismiss : TodoDetailIntent
}

sealed interface TodoDetailSideEffect : SideEffect {
    data class NavigateToEdit(val todoId: Long) : TodoDetailSideEffect
    data class ShowError(val message: String) : TodoDetailSideEffect
    data object Dismiss : TodoDetailSideEffect
}
