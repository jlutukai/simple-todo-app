package com.lutukai.simpletodoapp.domain.repository

import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.domain.models.Todo
import kotlinx.coroutines.flow.Flow

interface TodoRepository  {
    fun getAllTodos(): Flow<List<Todo>>

    suspend fun getTodoById(id: Long): Todo?

    suspend fun insertTodo(todo: Todo)

    suspend fun insertTodoWithId(todo: Todo): Long

    suspend fun updateTodo(todo: Todo)

    suspend fun deleteTodo(todo: Todo)

    suspend fun deleteCompletedTodos(): Int
}