package com.example.core.data.ai.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE conversation_id = :conversationId ORDER BY created_at ASC, id ASC")
    fun observeByConversation(conversationId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE conversation_id = :conversationId ORDER BY created_at ASC, id ASC")
    suspend fun getByConversation(conversationId: Long): List<ChatMessageEntity>

    @Insert
    suspend fun insert(message: ChatMessageEntity): Long
}
