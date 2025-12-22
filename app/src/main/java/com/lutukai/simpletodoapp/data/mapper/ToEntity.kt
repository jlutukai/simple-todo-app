package com.lutukai.simpletodoapp.data.mapper

import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.domain.models.Todo

fun Todo.toEntity(): TodoEntity = TodoEntity(
    id = id ?: 0,
    title = title,
    completedAt = completedAt,
    createdAt = createdAt ?: System.currentTimeMillis(),
    isCompleted = isCompleted,
    description = description
)