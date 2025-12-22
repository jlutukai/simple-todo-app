package com.lutukai.simpletodoapp.domain.models

data class Todo(
    val id: Long?,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val completedAt: Long?,
    val createdAt: Long?
)
