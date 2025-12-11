package com.lutukai.simpletodoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.data.local.entity.TodoEntity.Companion.TODO_TABLE_NAME
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

@Dao
interface TodoDao {

    // Returns Flowable - emits new list every time the table changes
    // This is the "reactive list" - automatically updates your UI
    @Query("SELECT * FROM $TODO_TABLE_NAME ORDER BY createdAt DESC")
    fun getAllTodos(): Flowable<List<TodoEntity>>

    // Returns Maybe - the todo might not exist
    // Emits the item if found, completes empty if not
    @Query("SELECT * FROM $TODO_TABLE_NAME WHERE id = :id")
    fun getTodoById(id: Long): Maybe<TodoEntity>

    // Alternative: Single throws an error if not found
    @Query("SELECT * FROM $TODO_TABLE_NAME WHERE id = :id")
    fun getTodoByIdOrError(id: Long): Single<TodoEntity>

    // Returns Completable - we only care about success/failure
    @Insert
    fun insertTodo(todo: TodoEntity): Completable

    // Returns Single<Long> - get the inserted row ID
    @Insert
    fun insertTodoWithId(todo: TodoEntity): Single<Long>

    // Update returns Completable
    @Update
    fun updateTodo(todo: TodoEntity): Completable

    // Delete returns Completable
    @Delete
    fun deleteTodo(todo: TodoEntity): Completable

    // You can also return the number of affected rows
    @Query("DELETE FROM $TODO_TABLE_NAME WHERE isCompleted = 1")
    fun deleteCompletedTodos(): Single<Int>
}