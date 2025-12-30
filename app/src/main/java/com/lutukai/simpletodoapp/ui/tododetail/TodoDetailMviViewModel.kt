package com.lutukai.simpletodoapp.ui.tododetail

import com.lutukai.simpletodoapp.domain.usecases.GetTodoByIdUseCase
import com.lutukai.simpletodoapp.domain.usecases.UpdateTodoUseCase
import com.lutukai.simpletodoapp.ui.mvi.MviViewModel
import com.lutukai.simpletodoapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TodoDetailMviViewModel @Inject constructor(
    private val getTodoByIdUseCase: GetTodoByIdUseCase,
    private val updateTodoUseCase: UpdateTodoUseCase
) : MviViewModel<TodoDetailState, TodoDetailIntent, TodoDetailSideEffect>(
    initialState = TodoDetailState()
) {

    override suspend fun handleIntent(intent: TodoDetailIntent) {
        when (intent) {
            is TodoDetailIntent.LoadTodo -> loadTodo(intent.todoId)
            is TodoDetailIntent.ToggleComplete -> toggleComplete(intent.isCompleted)
            is TodoDetailIntent.EditClicked -> {
                currentState.todo?.id?.let { id ->
                    sendEffect(TodoDetailSideEffect.NavigateToEdit(id))
                }
            }
            is TodoDetailIntent.Dismiss -> {
                sendEffect(TodoDetailSideEffect.Dismiss)
            }
        }
    }

    private suspend fun loadTodo(todoId: Long) {
        updateState { copy(isLoading = true, error = null) }

        when (val result = getTodoByIdUseCase(todoId)) {
            is Result.Success -> {
                val todo = result.data
                if (todo != null) {
                    updateState { copy(todo = todo, isLoading = false) }
                } else {
                    updateState { copy(isLoading = false, error = "Todo not found") }
                    sendEffect(TodoDetailSideEffect.ShowError("Todo not found"))
                }
            }
            is Result.Failure -> {
                updateState { copy(isLoading = false, error = result.message) }
                sendEffect(TodoDetailSideEffect.ShowError(result.message))
            }
        }
    }

    private suspend fun toggleComplete(isCompleted: Boolean) {
        val todo = currentState.todo ?: return

        val updatedTodo = todo.copy(
            isCompleted = isCompleted,
            completedAt = if (isCompleted) System.currentTimeMillis() else null
        )

        updateTodoUseCase(updatedTodo)
            .onSuccess {
                updateState { copy(todo = updatedTodo) }
            }
            .onFailure { exception, _ ->
                sendEffect(TodoDetailSideEffect.ShowError(exception.message ?: "Failed to update task"))
            }
    }
}
