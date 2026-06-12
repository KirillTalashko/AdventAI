package com.example.core.data.ai.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["conversation_id"])],
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "conversation_id")
    val conversationId: Long,
    @ColumnInfo(name = "author")
    val author: String,
    @ColumnInfo(name = "text")
    val text: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    // Токены ответа модели (только у сообщений агента; null у пользователя/приветствий).
    @ColumnInfo(name = "prompt_tokens")
    val promptTokens: Int? = null,
    @ColumnInfo(name = "completion_tokens")
    val completionTokens: Int? = null,
    @ColumnInfo(name = "total_tokens")
    val totalTokens: Int? = null,
    @ColumnInfo(name = "cache_hit_tokens")
    val cacheHitTokens: Int? = null,
    // Какой моделью получен ответ — для подсчёта стоимости в статистике.
    @ColumnInfo(name = "model_api_id")
    val modelApiId: String? = null
)
