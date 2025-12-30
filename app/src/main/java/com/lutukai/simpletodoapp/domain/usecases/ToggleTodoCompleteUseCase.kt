package com.lutukai.simpletodoapp.domain.usecases

import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import com.lutukai.simpletodoapp.util.Result
import javax.inject.Inject

class ToggleTodoCompleteUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(todo: Todo): Result<Todo> {
        val newCompletedState = !todo.isCompleted
        val updatedTodo = todo.copy(
            isCompleted = newCompletedState,
            completedAt = if (newCompletedState) System.currentTimeMillis() else null
        )
        return repository.updateTodo(updatedTodo).map { updatedTodo }
    }
}
