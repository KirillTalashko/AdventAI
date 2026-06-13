package com.example.core.data.ai.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ConversationEntity::class,
        ChatMessageEntity::class,
        FillerQuestionEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AdventAiDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun fillerQuestionDao(): FillerQuestionDao
}
