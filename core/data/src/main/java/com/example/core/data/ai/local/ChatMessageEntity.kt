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
    val createdAt: Long
)
