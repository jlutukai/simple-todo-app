package com.lutukai.simpletodoapp.data.repository

import com.lutukai.simpletodoapp.data.local.dao.TodoDao
import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.data.mapper.toDomain
import com.lutukai.simpletodoapp.data.mapper.toEntity
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.collections.map

class TodoRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao
) : TodoRepository{

    override fun getAllTodos(): Flow<List<Todo>> {
        return todoDao.getAllTodos().map { l-> l.map { it.toDomain() } }
    }

    override suspend fun getTodoById(id: Long): Todo? {
        return todoDao.getTodoById(id)?.toDomain()
    }

    override suspend fun insertTodo(todo: Todo) {
        todoDao.insertTodo(todo.toEntity())
    }

    override suspend fun insertTodoWithId(todo: Todo): Long {
        return todoDao.insertTodoWithId(todo.toEntity())
    }

    override suspend fun updateTodo(todo: Todo) {
        todoDao.updateTodo(todo.toEntity())
    }

    override suspend fun deleteTodo(todo: Todo) {
        todoDao.deleteTodo(todo.toEntity())
    }

    override suspend fun deleteCompletedTodos(): Int {
        return todoDao.deleteCompletedTodos()
    }
}