package com.lutukai.simpletodoapp.ui.mappers

import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.ui.model.TodoUi

fun Todo.toUI(): TodoUi = TodoUi(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    completedAt = completedAt,
    createdAt = createdAt
)