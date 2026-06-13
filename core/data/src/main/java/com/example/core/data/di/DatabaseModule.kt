package com.example.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.core.data.ai.local.AdventAiDatabase
import com.example.core.data.ai.local.ChatMessageDao
import com.example.core.data.ai.local.ConversationDao
import com.example.core.data.ai.local.FillerQuestionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DATABASE_NAME = "adventai.db"

    @Provides
    @Singleton
    fun provideAdventAiDatabase(
        @ApplicationContext context: Context
    ): AdventAiDatabase =
        Room.databaseBuilder(
            context,
            AdventAiDatabase::class.java,
            DATABASE_NAME
        )
            // Дев-приложение: при смене схемы пересоздаём БД, миграции не пишем.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideConversationDao(database: AdventAiDatabase): ConversationDao =
        database.conversationDao()

    @Provides
    fun provideChatMessageDao(database: AdventAiDatabase): ChatMessageDao =
        database.chatMessageDao()

    @Provides
    fun provideFillerQuestionDao(database: AdventAiDatabase): FillerQuestionDao =
        database.fillerQuestionDao()
}
