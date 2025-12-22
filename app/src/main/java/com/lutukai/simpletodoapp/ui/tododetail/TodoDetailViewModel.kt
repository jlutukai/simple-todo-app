package com.lutukai.simpletodoapp.ui.tododetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoDetailViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoDetailUiState())
    val uiState: StateFlow<TodoDetailUiState> = _uiState.asStateFlow()

    private val _events = Channel<TodoDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun loadTodo(todoId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val todo = repository.getTodoById(todoId)
                if (todo != null) {
                    _uiState.value = _uiState.value.copy(
                        todo = todo,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Todo not found"
                    )
                    _events.send(TodoDetailEvent.ShowError("Todo not found"))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An Unknown Error Occurred"
                )
                _events.send(TodoDetailEvent.ShowError(e.message ?: "An Unknown Error Occurred"))
            }
        }
    }

    fun toggleComplete(isCompleted: Boolean) {
        val todo = _uiState.value.todo ?: return

        viewModelScope.launch {
            try {
                val updatedTodo = todo.copy(
                    isCompleted = isCompleted,
                    completedAt = if (isCompleted) System.currentTimeMillis() else null
                )
                repository.updateTodo(updatedTodo)
                _uiState.value = _uiState.value.copy(todo = updatedTodo)
            } catch (e: Exception) {
                _events.send(TodoDetailEvent.ShowError(e.message ?: "Failed to update task"))
                // Revert by keeping current todo state
            }
        }
    }

    fun onEditClicked() {
        val todo = _uiState.value.todo ?: return
        val id = todo.id ?: return
        viewModelScope.launch {
            _events.send(TodoDetailEvent.NavigateToEdit(id))
        }
    }
}