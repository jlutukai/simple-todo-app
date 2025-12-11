package com.lutukai.simpletodoapp.data.repository

import android.util.Log
import com.lutukai.simpletodoapp.data.local.dao.TodoDao
import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.util.schedulerprovider.SchedulerProvider
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class TodoRepository @Inject constructor(
    private val todoDao: TodoDao,
    private val schedulerProvider: SchedulerProvider
) {

    companion object {
        private const val TAG = "TodoRepository"
    }

    fun getAllTodos(): Flowable<List<TodoEntity>> {
        return todoDao.getAllTodos()
            .subscribeOn(schedulerProvider.io())
            .doOnError { error ->
                Log.e(TAG, "Error fetching all todos", error)
            }
    }

    fun getTodoById(id: Long): Maybe<TodoEntity> {
        return todoDao.getTodoById(id)
            .subscribeOn(schedulerProvider.io())
            .doOnError { error ->
                Log.e(TAG, "Error fetching todo with id: $id", error)
            }
    }

    fun getTodoByIdOrError(id: Long): Single<TodoEntity> {
        return todoDao.getTodoByIdOrError(id)
            .subscribeOn(schedulerProvider.io())
            .doOnError { error ->
                Log.e(TAG, "Error fetching todo with id: $id", error)
            }
    }

    fun insertTodo(todo: TodoEntity): Completable {
        return todoDao.insertTodo(todo)
            .subscribeOn(schedulerProvider.io())
            .doOnError { error ->
                Log.e(TAG, "Error inserting todo: ${todo.title}", error)
            }
    }

    fun insertTodoWithId(todo: TodoEntity): Single<Long> {
        return todoDao.insertTodoWithId(todo)
            .subscribeOn(schedulerProvider.io())
            .doOnError { error ->
                Log.e(TAG, "Error inserting todo with id: ${todo.title}", error)
            }
    }

    fun updateTodo(todo: TodoEntity): Completable {
        return todoDao.updateTodo(todo)
            .subscribeOn(schedulerProvider.io())
            .doOnError { error ->
                Log.e(TAG, "Error updating todo: ${todo.id}", error)
            }
    }

    fun deleteTodo(todo: TodoEntity): Completable {
        return todoDao.deleteTodo(todo)
            .subscribeOn(schedulerProvider.io())
            .doOnError { error ->
                Log.e(TAG, "Error deleting todo: ${todo.id}", error)
            }
    }

    fun deleteCompletedTodos(): Single<Int> {
        return todoDao.deleteCompletedTodos()
            .subscribeOn(schedulerProvider.io())
            .doOnError { error ->
                Log.e(TAG, "Error deleting completed todos", error)
            }
    }
}