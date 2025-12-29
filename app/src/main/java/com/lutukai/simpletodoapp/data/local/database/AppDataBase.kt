package com.lutukai.simpletodoapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lutukai.simpletodoapp.data.local.dao.TodoDao
import com.lutukai.simpletodoapp.data.local.entity.TodoEntity

@Database(entities = [TodoEntity::class], version = 1)
abstract class AppDataBase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}