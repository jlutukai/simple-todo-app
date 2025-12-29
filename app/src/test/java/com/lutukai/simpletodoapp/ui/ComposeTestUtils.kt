package com.lutukai.simpletodoapp.ui

import com.lutukai.simpletodoapp.domain.models.Todo

/**
 * Factory functions for creating test data.
 */
object TestTodoFactory {
    fun createTodo(
        id: Long = 1L,
        title: String = "Test Todo",
        description: String = "Test Description",
        isCompleted: Boolean = false,
        completedAt: Long? = null,
        createdAt: Long = System.currentTimeMillis()
    ): Todo = Todo(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        completedAt = completedAt,
        createdAt = createdAt
    )

    fun createTodoList(count: Int = 3): List<Todo> = (1..count).map { i ->
        createTodo(
            id = i.toLong(),
            title = "Todo $i",
            description = "Description $i"
        )
    }

    fun createCompletedTodo(
        id: Long = 1L,
        title: String = "Completed Todo"
    ): Todo = createTodo(
        id = id,
        title = title,
        isCompleted = true,
        completedAt = System.currentTimeMillis()
    )
}
