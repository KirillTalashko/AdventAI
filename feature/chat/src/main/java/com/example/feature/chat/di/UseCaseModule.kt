package com.example.feature.chat.di

import com.example.feature.chat.domain.repository.ChatRepository
import com.example.feature.chat.domain.usecase.SendMessageUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideSendMessageUseCase(
        repository: ChatRepository
    ): SendMessageUseCase = SendMessageUseCase(repository)
}
