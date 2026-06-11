package com.example.core.data.ai.mapper

import com.example.core.data.ai.local.ChatMessageEntity
import com.example.core.data.ai.local.ConversationEntity
import com.example.core.model.ai.AgentChatMessage
import com.example.core.model.ai.AgentMessageAuthor
import com.example.core.model.ai.Conversation

fun ConversationEntity.toDomain(): Conversation =
    Conversation(
        id = id,
        agentId = agentId,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun ChatMessageEntity.toDomain(): AgentChatMessage =
    AgentChatMessage(
        author = runCatching { AgentMessageAuthor.valueOf(author) }
            .getOrDefault(AgentMessageAuthor.Agent),
        text = text,
        createdAt = createdAt
    )

fun AgentChatMessage.toEntity(
    conversationId: Long,
    createdAt: Long = this.createdAt.takeIf { it > 0L } ?: System.currentTimeMillis()
): ChatMessageEntity =
    ChatMessageEntity(
        conversationId = conversationId,
        author = author.name,
        text = text,
        createdAt = createdAt
    )
