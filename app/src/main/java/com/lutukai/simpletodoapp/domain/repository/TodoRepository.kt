package com.lutukai.simpletodoapp.domain.repository

import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.util.Result
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun getAllTodos(): Flow<List<Todo>>

    suspend fun getTodoById(id: Long): Result<Todo?>

    suspend fun insertTodo(todo: Todo): Result<Unit>

    suspend fun insertTodoWithId(todo: Todo): Result<Long>

    suspend fun updateTodo(todo: Todo): Result<Unit>

    suspend fun deleteTodo(todo: Todo): Result<Unit>

    suspend fun deleteCompletedTodos(): Result<Int>
}