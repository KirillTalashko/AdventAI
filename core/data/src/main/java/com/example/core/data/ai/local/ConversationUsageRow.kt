package com.example.core.data.ai.local

import androidx.room.ColumnInfo

/** Проекция агрегата токенов по одному диалогу (результат JOIN + GROUP BY). */
data class ConversationUsageRow(
    @ColumnInfo(name = "conversation_id")
    val conversationId: Long,
    @ColumnInfo(name = "agent_id")
    val agentId: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "prompt_tokens")
    val promptTokens: Int,
    @ColumnInfo(name = "completion_tokens")
    val completionTokens: Int,
    @ColumnInfo(name = "total_tokens")
    val totalTokens: Int,
    @ColumnInfo(name = "cache_hit_tokens")
    val cacheHitTokens: Int,
    @ColumnInfo(name = "message_count")
    val messageCount: Int,
    @ColumnInfo(name = "model_api_id")
    val modelApiId: String?
)
