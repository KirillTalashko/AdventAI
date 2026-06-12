package com.example.core.data.ai.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE agent_id = :agentId ORDER BY updated_at DESC")
    fun observeByAgent(agentId: String): Flow<List<ConversationEntity>>

    @Query("SELECT id FROM conversations WHERE agent_id = :agentId ORDER BY updated_at DESC LIMIT 1")
    suspend fun latestIdForAgent(agentId: String): Long?

    @Insert
    suspend fun insert(conversation: ConversationEntity): Long

    @Query("UPDATE conversations SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: Long, title: String)

    @Query("UPDATE conversations SET updated_at = :updatedAt WHERE id = :id")
    suspend fun touch(id: Long, updatedAt: Long)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun delete(id: Long)

    /**
     * Сводка по токенам каждого диалога: суммируем токены сообщений с учтённым
     * расходом (ответы модели). LEFT JOIN сохраняет диалоги без статистики с нулями.
     */
    @Query(
        """
        SELECT
            c.id AS conversation_id,
            c.agent_id AS agent_id,
            c.title AS title,
            c.updated_at AS updated_at,
            COALESCE(SUM(m.prompt_tokens), 0) AS prompt_tokens,
            COALESCE(SUM(m.completion_tokens), 0) AS completion_tokens,
            COALESCE(SUM(m.total_tokens), 0) AS total_tokens,
            COALESCE(SUM(m.cache_hit_tokens), 0) AS cache_hit_tokens,
            COUNT(m.id) AS message_count,
            MAX(m.model_api_id) AS model_api_id
        FROM conversations c
        LEFT JOIN chat_messages m
            ON m.conversation_id = c.id AND m.total_tokens IS NOT NULL
        GROUP BY c.id
        ORDER BY c.updated_at DESC
        """
    )
    fun observeTokenStats(): Flow<List<ConversationUsageRow>>
}
