package com.lutukai.simpletodoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lutukai.simpletodoapp.data.local.entity.TodoEntity.Companion.TODO_TABLE_NAME

@Entity(tableName = TODO_TABLE_NAME)
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val completedAt: Long?,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TODO_TABLE_NAME = "todos"
    }
}
