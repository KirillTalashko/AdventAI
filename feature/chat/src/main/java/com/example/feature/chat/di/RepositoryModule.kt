package com.example.feature.chat.di

import com.example.feature.chat.data.repository.ChatRepositoryImpl
import com.example.feature.chat.domain.repository.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindChatRepository(
        implementation: ChatRepositoryImpl
    ): ChatRepository
}
