package com.example.core.data.ai.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [Index(value = ["agent_id"])]
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "agent_id")
    val agentId: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    /**
     * Эфемерный диалог демо-заливки контекста: не показывается в списке диалогов и статистике,
     * удаляется в конце сессии.
     */
    @ColumnInfo(name = "ephemeral")
    val ephemeral: Boolean = false
)
