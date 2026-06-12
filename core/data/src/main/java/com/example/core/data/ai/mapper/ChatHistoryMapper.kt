package com.example.core.data.ai.mapper

import com.example.core.data.ai.local.ChatMessageEntity
import com.example.core.data.ai.local.ConversationEntity
import com.example.core.data.ai.local.ConversationUsageRow
import com.example.core.model.ai.AgentChatMessage
import com.example.core.model.ai.AgentMessageAuthor
import com.example.core.model.ai.Conversation
import com.example.core.model.ai.ConversationTokenStat
import com.example.core.model.ai.TokenUsage

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
        createdAt = createdAt,
        usage = toTokenUsage(),
        modelApiId = modelApiId
    )

private fun ChatMessageEntity.toTokenUsage(): TokenUsage? {
    val total = totalTokens ?: return null
    val prompt = promptTokens ?: 0
    return TokenUsage(
        promptTokens = prompt,
        completionTokens = completionTokens ?: 0,
        totalTokens = total,
        promptCacheHitTokens = cacheHitTokens ?: 0,
        promptCacheMissTokens = (prompt - (cacheHitTokens ?: 0)).coerceAtLeast(0)
    )
}

fun AgentChatMessage.toEntity(
    conversationId: Long,
    createdAt: Long = this.createdAt.takeIf { it > 0L } ?: System.currentTimeMillis()
): ChatMessageEntity =
    ChatMessageEntity(
        conversationId = conversationId,
        author = author.name,
        text = text,
        createdAt = createdAt,
        promptTokens = usage?.promptTokens,
        completionTokens = usage?.completionTokens,
        totalTokens = usage?.totalTokens,
        cacheHitTokens = usage?.promptCacheHitTokens,
        modelApiId = modelApiId
    )

fun ConversationUsageRow.toDomain(): ConversationTokenStat =
    ConversationTokenStat(
        conversationId = conversationId,
        agentId = agentId,
        title = title,
        updatedAt = updatedAt,
        promptTokens = promptTokens,
        completionTokens = completionTokens,
        totalTokens = totalTokens,
        cacheHitTokens = cacheHitTokens,
        messageCount = messageCount,
        modelApiId = modelApiId
    )
