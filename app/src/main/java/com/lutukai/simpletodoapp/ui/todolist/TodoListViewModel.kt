package com.lutukai.simpletodoapp.ui.todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<TodoListUiState> =
        MutableStateFlow(TodoListUiState())
    val uiState: StateFlow<TodoListUiState> = _uiState.asStateFlow()

    private val _events = Channel<TodoListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadTodos()
    }

    fun loadTodos() {
        viewModelScope.launch {
            repository.getAllTodos()
                .onStart {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                }
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown Error Occurred"
                    )
                }
                .collect { todos ->
                    _uiState.value = _uiState.value.copy(
                        todos = todos,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun addNewTodo() {
        viewModelScope.launch {
            _events.send(TodoListEvent.NavigateToAddTodo)
        }
    }

    fun openTodoDetail(todo: Todo) {
        val id = todo.id ?: return
        viewModelScope.launch {
            _events.send(TodoListEvent.NavigateToDetail(id))
        }
    }

    fun toggleComplete(todo: Todo) {
        viewModelScope.launch {
            try {
                val newCompletedState = !todo.isCompleted
                repository.updateTodo(
                    todo.copy(
                        completedAt = if (newCompletedState) System.currentTimeMillis() else null,
                        isCompleted = newCompletedState
                    )
                )
            } catch (e: Exception) {
                _events.send(TodoListEvent.ShowSnackbar("Update failed"))
            }
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.deleteTodo(todo)
                _events.send(
                    TodoListEvent.ShowSnackbar(
                        message = "Task deleted",
                        actionLabel = "Undo",
                        action = { undoDelete(todo) }
                    )
                )
            } catch (e: Exception) {
                _events.send(TodoListEvent.ShowSnackbar("Delete failed"))
            }
        }
    }

    fun undoDelete(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.insertTodo(todo)
            } catch (e: Exception) {
                _events.send(TodoListEvent.ShowSnackbar("Restore failed"))
            }
        }
    }
}