package com.lutukai.simpletodoapp.ui.model

data class TodoUi(
    val id: Long?,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val completedAt: Long?,
    val createdAt: Long?
)
