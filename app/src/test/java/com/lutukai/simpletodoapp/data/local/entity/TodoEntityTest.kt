package com.lutukai.simpletodoapp.data.local.entity

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TodoEntityTest {

    // ============== DEFAULT VALUES TESTS ==============

    @Test
    fun `default id is 0`() {
        val todo = TodoEntity(
            title = "Test",
            completedAt = null
        )

        assertThat(todo.id).isEqualTo(0)
    }

    @Test
    fun `default description is empty string`() {
        val todo = TodoEntity(
            title = "Test",
            completedAt = null
        )

        assertThat(todo.description).isEmpty()
    }

    @Test
    fun `default isCompleted is false`() {
        val todo = TodoEntity(
            title = "Test",
            completedAt = null
        )

        assertThat(todo.isCompleted).isFalse()
    }

    @Test
    fun `createdAt has default value`() {
        val beforeCreation = System.currentTimeMillis()
        val todo = TodoEntity(
            title = "Test",
            completedAt = null
        )
        val afterCreation = System.currentTimeMillis()

        assertThat(todo.createdAt).isAtLeast(beforeCreation)
        assertThat(todo.createdAt).isAtMost(afterCreation)
    }

    // ============== COPY FUNCTION TESTS ==============

    @Test
    fun `copy preserves unchanged fields`() {
        val original = TodoEntity(
            id = 1,
            title = "Original Title",
            description = "Original Description",
            isCompleted = false,
            completedAt = null,
            createdAt = 1000L
        )

        val copied = original.copy(title = "New Title")

        assertThat(copied.id).isEqualTo(original.id)
        assertThat(copied.description).isEqualTo(original.description)
        assertThat(copied.isCompleted).isEqualTo(original.isCompleted)
        assertThat(copied.completedAt).isEqualTo(original.completedAt)
        assertThat(copied.createdAt).isEqualTo(original.createdAt)
        assertThat(copied.title).isEqualTo("New Title")
    }

    @Test
    fun `copy updates specified fields`() {
        val original = TodoEntity(
            id = 1,
            title = "Test",
            description = "",
            isCompleted = false,
            completedAt = null,
            createdAt = 1000L
        )

        val copied = original.copy(
            isCompleted = true,
            completedAt = 2000L
        )

        assertThat(copied.isCompleted).isTrue()
        assertThat(copied.completedAt).isEqualTo(2000L)
    }

    @Test
    fun `copy creates new instance`() {
        val original = TodoEntity(
            id = 1,
            title = "Test",
            completedAt = null
        )

        val copied = original.copy()

        assertThat(copied).isNotSameInstanceAs(original)
        assertThat(copied).isEqualTo(original)
    }

    // ============== TABLE NAME CONSTANT TEST ==============

    @Test
    fun `table name constant is correct`() {
        assertThat(TodoEntity.TODO_TABLE_NAME).isEqualTo("todos")
    }

    // ============== DATA CLASS EQUALITY TESTS ==============

    @Test
    fun `equals returns true for identical data`() {
        val todo1 = TodoEntity(
            id = 1,
            title = "Test",
            description = "Desc",
            isCompleted = true,
            completedAt = 1000L,
            createdAt = 500L
        )
        val todo2 = TodoEntity(
            id = 1,
            title = "Test",
            description = "Desc",
            isCompleted = true,
            completedAt = 1000L,
            createdAt = 500L
        )

        assertThat(todo1).isEqualTo(todo2)
        assertThat(todo1.hashCode()).isEqualTo(todo2.hashCode())
    }

    @Test
    fun `equals returns false for different data`() {
        val todo1 = TodoEntity(
            id = 1,
            title = "Test",
            completedAt = null
        )
        val todo2 = TodoEntity(
            id = 2,
            title = "Test",
            completedAt = null
        )

        assertThat(todo1).isNotEqualTo(todo2)
    }
}