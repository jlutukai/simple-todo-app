package com.lutukai.simpletodoapp.ui.features.todolist

import androidx.compose.runtime.Stable
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.ui.mvi.SideEffect
import com.lutukai.simpletodoapp.ui.mvi.UiIntent
import com.lutukai.simpletodoapp.ui.mvi.UiState

@Stable
data class TodoListState(
    val todos: List<Todo> = emptyList(),
    val filteredTodos: List<Todo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val filter: TodoFilter = TodoFilter.ALL
) : UiState {

    enum class TodoFilter {
        ALL, COMPLETED
    }

    companion object {
        fun computeFilteredTodos(
            todos: List<Todo>,
            filter: TodoFilter,
            searchQuery: String
        ): List<Todo> {
            var result = todos

            if (filter == TodoFilter.COMPLETED) {
                result = result.filter { it.isCompleted }
            }

            if (searchQuery.isNotBlank()) {
                result = result.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
                }
            }

            return result
        }
    }
}

sealed interface TodoListIntent : UiIntent {
    data object LoadTodos : TodoListIntent
    data class UpdateSearchQuery(val query: String) : TodoListIntent
    data class UpdateFilter(val filter: TodoListState.TodoFilter) : TodoListIntent
    data class ToggleComplete(val todo: Todo) : TodoListIntent
    data class DeleteTodo(val todo: Todo) : TodoListIntent
    data class UndoDelete(val todo: Todo) : TodoListIntent
    data class OpenTodoDetail(val todoId: Long) : TodoListIntent
    data object AddNewTodo : TodoListIntent
}

sealed interface TodoListSideEffect : SideEffect {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val todo: Todo? = null
    ) : TodoListSideEffect

    data class NavigateToDetail(val todoId: Long) : TodoListSideEffect
    data object NavigateToAddTodo : TodoListSideEffect
}
