package com.lutukai.simpletodoapp.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes using Kotlin Serialization
 */
@Serializable
sealed interface NavRoute {

    @Serializable
    data object TodoList : NavRoute

    @Serializable
    data class TodoDetail(val todoId: Long) : NavRoute

    @Serializable
    data class AddEditTodo(val todoId: Long? = null) : NavRoute {
        val isEditMode: Boolean get() = todoId != null
    }
}
