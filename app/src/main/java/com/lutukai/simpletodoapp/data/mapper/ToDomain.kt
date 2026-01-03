package com.lutukai.simpletodoapp.data.mapper

import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.domain.models.Todo

fun TodoEntity.toDomain(): Todo = Todo(
    id = id,
    title = title,
    completedAt = completedAt,
    createdAt = createdAt,
    isCompleted = isCompleted,
    description = description
)
