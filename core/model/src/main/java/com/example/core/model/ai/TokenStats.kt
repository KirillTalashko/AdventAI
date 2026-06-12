package com.example.core.model.ai

/**
 * Сводка по токенам одного диалога — агрегат по всем его сообщениям с агентом.
 * Используется на экране «Статистика» для сравнения коротких и длинных диалогов
 * и подсчёта стоимости.
 */
data class ConversationTokenStat(
    val conversationId: Long,
    val agentId: String,
    val title: String,
    val updatedAt: Long,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val cacheHitTokens: Int,
    /** Количество ответов модели (запросов с учтёнными токенами). */
    val messageCount: Int,
    val modelApiId: String?
)
