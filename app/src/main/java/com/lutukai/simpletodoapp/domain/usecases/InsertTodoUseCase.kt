package com.lutukai.simpletodoapp.domain.usecases

import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import com.lutukai.simpletodoapp.util.Result
import javax.inject.Inject

class InsertTodoUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(todo: Todo): Result<Unit> {
        return repository.insertTodo(todo)
    }
}
