package com.lutukai.simpletodoapp.ui.tododetail

import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import com.lutukai.simpletodoapp.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TodoDetailMviViewModel @Inject constructor(
    private val repository: TodoRepository
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
        try {
            val todo = repository.getTodoById(todoId)
            if (todo != null) {
                updateState { copy(todo = todo, isLoading = false) }
            } else {
                updateState { copy(isLoading = false, error = "Todo not found") }
                sendEffect(TodoDetailSideEffect.ShowError("Todo not found"))
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "An Unknown Error Occurred"
            updateState { copy(isLoading = false, error = errorMsg) }
            sendEffect(TodoDetailSideEffect.ShowError(errorMsg))
        }
    }

    private suspend fun toggleComplete(isCompleted: Boolean) {
        val todo = currentState.todo ?: return
        try {
            val updatedTodo = todo.copy(
                isCompleted = isCompleted,
                completedAt = if (isCompleted) System.currentTimeMillis() else null
            )
            repository.updateTodo(updatedTodo)
            updateState { copy(todo = updatedTodo) }
        } catch (e: Exception) {
            sendEffect(TodoDetailSideEffect.ShowError(e.message ?: "Failed to update task"))
        }
    }
}
