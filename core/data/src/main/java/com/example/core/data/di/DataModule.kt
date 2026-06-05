package com.example.core.data.di

import com.example.core.common.DefaultDispatcherProvider
import com.example.core.common.DispatcherProvider
import com.example.core.data.ai.repository.AiChatRepositoryImpl
import com.example.core.domain.repository.AiChatRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindAiChatRepository(
        implementation: AiChatRepositoryImpl
    ): AiChatRepository

    companion object {
        @Provides
        @JvmStatic
        fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider
    }
}
