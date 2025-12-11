package com.lutukai.simpletodoapp.di

import android.content.Context
import androidx.room.Room
import com.lutukai.simpletodoapp.data.local.dao.TodoDao
import com.lutukai.simpletodoapp.data.local.database.AppDataBase
import com.lutukai.simpletodoapp.util.Constants.DB_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDataBase {
        return Room.databaseBuilder(
            context,
            AppDataBase::class.java,
            DB_NAME
        ).build()
    }

    @Provides
    fun provideTodoDao(database: AppDataBase): TodoDao = database.todoDao()
}