package com.lutukai.simpletodoapp.domain.usecases

import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import com.lutukai.simpletodoapp.util.Result
import javax.inject.Inject

class DeleteCompletedTodosUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return repository.deleteCompletedTodos()
    }
}
