package com.lutukai.simpletodoapp.domain.usecases

import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import javax.inject.Inject

class DeleteCompletedTodosUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(): Int {
        return repository.deleteCompletedTodos()
    }
}
