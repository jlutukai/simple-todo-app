package com.lutukai.simpletodoapp.data.repository

import com.lutukai.simpletodoapp.data.local.dao.TodoDao
import com.lutukai.simpletodoapp.data.mapper.toDomain
import com.lutukai.simpletodoapp.data.mapper.toEntity
import com.lutukai.simpletodoapp.di.AppDispatchers
import com.lutukai.simpletodoapp.di.Dispatcher
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import com.lutukai.simpletodoapp.util.Result
import com.lutukai.simpletodoapp.util.safeCall
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TodoRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : TodoRepository {

    override fun getAllTodos(): Flow<List<Todo>> = todoDao.getAllTodos().map { list -> list.map { it.toDomain() } }

    override suspend fun getTodoById(id: Long): Result<Todo?> = safeCall(ioDispatcher) {
        todoDao.getTodoById(id)?.toDomain()
    }

    override suspend fun insertTodo(todo: Todo): Result<Unit> = safeCall(ioDispatcher) {
        todoDao.insertTodo(todo.toEntity())
    }

    override suspend fun insertTodoWithId(todo: Todo): Result<Long> = safeCall(ioDispatcher) {
        todoDao.insertTodoWithId(todo.toEntity())
    }

    override suspend fun updateTodo(todo: Todo): Result<Unit> = safeCall(ioDispatcher) {
        todoDao.updateTodo(todo.toEntity())
    }

    override suspend fun deleteTodo(todo: Todo): Result<Unit> = safeCall(ioDispatcher) {
        todoDao.deleteTodo(todo.toEntity())
    }

    override suspend fun deleteCompletedTodos(): Result<Int> = safeCall(ioDispatcher) { todoDao.deleteCompletedTodos() }
}
