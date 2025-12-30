package com.lutukai.simpletodoapp.ui.addedittodo

import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.usecases.GetTodoByIdUseCase
import com.lutukai.simpletodoapp.domain.usecases.InsertTodoUseCase
import com.lutukai.simpletodoapp.ui.mvi.MviViewModel
import com.lutukai.simpletodoapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddEditTodoMviViewModel @Inject constructor(
    private val getTodoByIdUseCase: GetTodoByIdUseCase,
    private val insertTodoUseCase: InsertTodoUseCase
) : MviViewModel<AddEditTodoState, AddEditTodoIntent, AddEditTodoSideEffect>(
    initialState = AddEditTodoState()
) {

    override suspend fun handleIntent(intent: AddEditTodoIntent) {
        when (intent) {
            is AddEditTodoIntent.LoadTodo -> loadTodo(intent.todoId)
            is AddEditTodoIntent.UpdateTitle -> updateState { copy(title = intent.title) }
            is AddEditTodoIntent.UpdateDescription -> updateState { copy(description = intent.description) }
            is AddEditTodoIntent.UpdateCompleted -> updateState { copy(isCompleted = intent.isCompleted) }
            is AddEditTodoIntent.SaveTodo -> saveTodo()
            is AddEditTodoIntent.Cancel -> sendEffect(AddEditTodoSideEffect.Dismiss)
        }
    }

    private suspend fun loadTodo(todoId: Long) {
        updateState { copy(isLoading = true, error = null, isEditMode = true) }

        when (val result = getTodoByIdUseCase(todoId)) {
            is Result.Success -> {
                val todo = result.data
                if (todo != null) {
                    updateState {
                        copy(
                            todo = todo,
                            title = todo.title,
                            description = todo.description,
                            isCompleted = todo.isCompleted,
                            isLoading = false
                        )
                    }
                } else {
                    updateState { copy(isLoading = false, error = "Todo not found") }
                    sendEffect(AddEditTodoSideEffect.ShowError("Todo not found"))
                }
            }
            is Result.Failure -> {
                updateState { copy(isLoading = false, error = result.message) }
                sendEffect(AddEditTodoSideEffect.ShowError(result.message))
            }
        }
    }

    private suspend fun saveTodo() {
        val state = currentState
        if (state.title.isBlank()) {
            sendEffect(AddEditTodoSideEffect.ShowError("Title is required"))
            return
        }

        updateState { copy(isLoading = true, error = null) }

        val todoToSave = Todo(
            id = state.todo?.id,
            title = state.title.trim(),
            description = state.description.trim(),
            isCompleted = state.isCompleted,
            completedAt = if (state.isCompleted) System.currentTimeMillis() else null,
            createdAt = state.todo?.createdAt ?: System.currentTimeMillis()
        )

        insertTodoUseCase(todoToSave)
            .onSuccess {
                updateState { copy(isLoading = false) }
                sendEffect(AddEditTodoSideEffect.SaveSuccess)
            }
            .onFailure { _, message ->
                updateState { copy(isLoading = false, error = message) }
                sendEffect(AddEditTodoSideEffect.ShowError(message))
            }
    }
}
