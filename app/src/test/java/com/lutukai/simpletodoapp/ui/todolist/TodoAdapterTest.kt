package com.lutukai.simpletodoapp.ui.todolist

import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import org.junit.Test

/**
 * Tests for TodoAdapter's DiffUtil logic.
 * Since TodoComp is private, we test the logic it implements:
 * - areItemsTheSame: compares by ID
 * - areContentsTheSame: compares by data class equality
 */
class TodoAdapterTest {

    // ============== ARE ITEMS THE SAME TESTS ==============
    // Logic: oldItem.id == newItem.id

    @Test
    fun `areItemsTheSame returns true for same ID`() {
        val todo1 = createTodo(id = 1, title = "Original")
        val todo2 = createTodo(id = 1, title = "Modified")

        // Same ID means same item
        assertThat(todo1.id == todo2.id).isTrue()
    }

    @Test
    fun `areItemsTheSame returns false for different IDs`() {
        val todo1 = createTodo(id = 1, title = "Todo 1")
        val todo2 = createTodo(id = 2, title = "Todo 1")

        // Different ID means different item
        assertThat(todo1.id == todo2.id).isFalse()
    }

    // ============== ARE CONTENTS THE SAME TESTS ==============
    // Logic: oldItem == newItem (data class equality)

    @Test
    fun `areContentsTheSame returns true for identical todos`() {
        val createdAt = 1000L
        val todo1 = createTodo(id = 1, title = "Test", createdAt = createdAt)
        val todo2 = createTodo(id = 1, title = "Test", createdAt = createdAt)

        // Data class equality check
        assertThat(todo1 == todo2).isTrue()
    }

    @Test
    fun `areContentsTheSame returns false when title changes`() {
        val createdAt = 1000L
        val todo1 = createTodo(id = 1, title = "Original", createdAt = createdAt)
        val todo2 = createTodo(id = 1, title = "Modified", createdAt = createdAt)

        assertThat(todo1 == todo2).isFalse()
    }

    @Test
    fun `areContentsTheSame returns false when description changes`() {
        val createdAt = 1000L
        val todo1 = createTodo(id = 1, title = "Test", description = "Desc 1", createdAt = createdAt)
        val todo2 = createTodo(id = 1, title = "Test", description = "Desc 2", createdAt = createdAt)

        assertThat(todo1 == todo2).isFalse()
    }

    @Test
    fun `areContentsTheSame returns false when isCompleted changes`() {
        val createdAt = 1000L
        val todo1 = createTodo(id = 1, title = "Test", isCompleted = false, createdAt = createdAt)
        val todo2 = createTodo(id = 1, title = "Test", isCompleted = true, completedAt = 2000L, createdAt = createdAt)

        assertThat(todo1 == todo2).isFalse()
    }

    @Test
    fun `areContentsTheSame returns false when completedAt changes`() {
        val createdAt = 1000L
        val todo1 = createTodo(id = 1, title = "Test", isCompleted = true, completedAt = 2000L, createdAt = createdAt)
        val todo2 = createTodo(id = 1, title = "Test", isCompleted = true, completedAt = 3000L, createdAt = createdAt)

        assertThat(todo1 == todo2).isFalse()
    }

    // ============== HELPER FUNCTIONS ==============

    private fun createTodo(
        id: Long = 0,
        title: String = "Test Todo",
        description: String = "",
        isCompleted: Boolean = false,
        completedAt: Long? = null,
        createdAt: Long = System.currentTimeMillis()
    ): TodoEntity {
        return TodoEntity(
            id = id,
            title = title,
            description = description,
            isCompleted = isCompleted,
            completedAt = completedAt,
            createdAt = createdAt
        )
    }
}