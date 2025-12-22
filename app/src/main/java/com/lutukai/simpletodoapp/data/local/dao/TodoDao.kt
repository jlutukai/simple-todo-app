package com.lutukai.simpletodoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.data.local.entity.TodoEntity.Companion.TODO_TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    // Returns Flow - emits new list every time the table changes
    // This is the "reactive list" - automatically updates your UI
    @Query("SELECT * FROM $TODO_TABLE_NAME ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    // Returns nullable - the todo might not exist
    @Query("SELECT * FROM $TODO_TABLE_NAME WHERE id = :id")
    suspend fun getTodoById(id: Long): TodoEntity?

    // Insert todo
    @Insert
    suspend fun insertTodo(todo: TodoEntity)

    // Insert and get the inserted row ID
    @Insert
    suspend fun insertTodoWithId(todo: TodoEntity): Long

    // Update todo
    @Update
    suspend fun updateTodo(todo: TodoEntity)

    // Delete todo
    @Delete
    suspend fun deleteTodo(todo: TodoEntity)

    // Delete completed todos and return the number of affected rows
    @Query("DELETE FROM $TODO_TABLE_NAME WHERE isCompleted = 1")
    suspend fun deleteCompletedTodos(): Int
}