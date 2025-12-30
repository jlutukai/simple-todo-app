package com.lutukai.simpletodoapp.ui.todolist

import androidx.lifecycle.viewModelScope
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.usecases.DeleteTodoUseCase
import com.lutukai.simpletodoapp.domain.usecases.GetAllTodosUseCase
import com.lutukai.simpletodoapp.domain.usecases.InsertTodoUseCase
import com.lutukai.simpletodoapp.domain.usecases.ToggleTodoCompleteUseCase
import com.lutukai.simpletodoapp.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoListMviViewModel @Inject constructor(
    private val getAllTodosUseCase: GetAllTodosUseCase,
    private val toggleTodoCompleteUseCase: ToggleTodoCompleteUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase,
    private val insertTodoUseCase: InsertTodoUseCase
) : MviViewModel<TodoListState, TodoListIntent, TodoListSideEffect>(
    initialState = TodoListState()
) {

    init {
        loadTodos()
    }

    override suspend fun handleIntent(intent: TodoListIntent) {
        when (intent) {
            is TodoListIntent.LoadTodos -> loadTodos()
            is TodoListIntent.UpdateSearchQuery -> updateStateWithFilteredTodos { copy(searchQuery = intent.query) }
            is TodoListIntent.UpdateFilter -> updateStateWithFilteredTodos { copy(filter = intent.filter) }
            is TodoListIntent.ToggleComplete -> toggleComplete(intent.todo)
            is TodoListIntent.DeleteTodo -> deleteTodo(intent.todo)
            is TodoListIntent.UndoDelete -> undoDelete(intent.todo)
            is TodoListIntent.OpenTodoDetail -> {
                sendEffect(TodoListSideEffect.NavigateToDetail(intent.todoId))
            }
            is TodoListIntent.AddNewTodo -> {
                sendEffect(TodoListSideEffect.NavigateToAddTodo)
            }
        }
    }

    private fun updateStateWithFilteredTodos(reducer: TodoListState.() -> TodoListState) {
        updateState {
            val newState = reducer()
            newState.copy(
                filteredTodos = TodoListState.computeFilteredTodos(
                    newState.todos,
                    newState.filter,
                    newState.searchQuery
                )
            )
        }
    }

    private fun loadTodos() {
        viewModelScope.launch {
            getAllTodosUseCase()
                .onStart {
                    updateState { copy(isLoading = true, error = null) }
                }
                .catch { error ->
                    updateState {
                        copy(isLoading = false, error = error.message ?: "Unknown Error")
                    }
                }
                .collect { todos ->
                    updateStateWithFilteredTodos { copy(todos = todos, isLoading = false, error = null) }
                }
        }
    }

    private suspend fun toggleComplete(todo: Todo) {
        toggleTodoCompleteUseCase(todo)
            .onFailure { _, _ ->
                sendEffect(TodoListSideEffect.ShowSnackbar("Update failed"))
            }
    }

    private suspend fun deleteTodo(todo: Todo) {
        deleteTodoUseCase(todo)
            .onSuccess {
                sendEffect(
                    TodoListSideEffect.ShowSnackbar(
                        message = "Task deleted",
                        actionLabel = "Undo",
                        todo = todo
                    )
                )
            }
            .onFailure { _, _ ->
                sendEffect(TodoListSideEffect.ShowSnackbar("Delete failed"))
            }
    }

    private suspend fun undoDelete(todo: Todo) {
        insertTodoUseCase(todo)
            .onFailure { _, _ ->
                sendEffect(TodoListSideEffect.ShowSnackbar("Restore failed"))
            }
    }
}
