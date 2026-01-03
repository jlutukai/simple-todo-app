package com.lutukai.simpletodoapp.domain.usecases

import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetAllTodosUseCase @Inject constructor(private val repository: TodoRepository) {
    operator fun invoke(): Flow<List<Todo>> = repository.getAllTodos()
}
