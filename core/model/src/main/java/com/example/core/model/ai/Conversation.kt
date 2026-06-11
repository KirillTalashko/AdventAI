package com.example.core.model.ai

/**
 * Отдельный диалог («тема») с агентом. У одного агента может быть много диалогов,
 * между которыми можно переключаться (как чаты в ChatGPT).
 */
data class Conversation(
    val id: Long,
    val agentId: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)
