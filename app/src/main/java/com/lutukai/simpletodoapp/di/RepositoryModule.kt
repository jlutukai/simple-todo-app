package com.lutukai.simpletodoapp.di

import com.lutukai.simpletodoapp.util.schedulerprovider.AppSchedulerProvider
import com.lutukai.simpletodoapp.util.schedulerprovider.SchedulerProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSchedulerProvider(): SchedulerProvider = AppSchedulerProvider()
}
