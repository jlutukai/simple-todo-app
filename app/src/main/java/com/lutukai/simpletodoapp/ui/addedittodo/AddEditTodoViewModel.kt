package com.lutukai.simpletodoapp.ui.addedittodo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lutukai.simpletodoapp.domain.models.Todo
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
class AddEditTodoViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditTodoUiState())
    val uiState: StateFlow<AddEditTodoUiState> = _uiState.asStateFlow()

    private val _events = Channel<AddEditTodoEvent>(Channel.BUFFERED)
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
                    _events.send(AddEditTodoEvent.ShowError("Todo not found"))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An Unknown Error Occurred"
                )
                _events.send(AddEditTodoEvent.ShowError(e.message ?: "An Unknown Error Occurred"))
            }
        }
    }

    fun saveTodo(
        title: String,
        description: String,
        isCompleted: Boolean,
        existingId: Long?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repository.insertTodo(
                    Todo(
                        id = existingId,
                        title = title,
                        description = description,
                        isCompleted = isCompleted,
                        completedAt = if (isCompleted) System.currentTimeMillis() else null,
                        createdAt = System.currentTimeMillis()
                    )
                )
                _uiState.value = _uiState.value.copy(isLoading = false)
                _events.send(AddEditTodoEvent.SaveSuccess)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An Unknown Error Occurred"
                )
                _events.send(AddEditTodoEvent.ShowError(e.message ?: "An Unknown Error Occurred"))
            }
        }
    }
}