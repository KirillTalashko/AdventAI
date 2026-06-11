package com.example.core.domain.repository

import com.example.core.common.AppResult
import com.example.core.model.ai.ChatMessage
import com.example.core.model.ai.ChatRequestOptions
import com.example.core.model.ai.LlmAnswer

interface AiChatRepository {
    suspend fun sendMessage(
        message: String,
        options: ChatRequestOptions = ChatRequestOptions()
    ): AppResult<String>

    suspend fun sendDetailedMessage(
        message: String,
        options: ChatRequestOptions = ChatRequestOptions()
    ): AppResult<LlmAnswer>

    suspend fun sendOpenRouterDetailedMessage(
        message: String,
        modelId: String,
        options: ChatRequestOptions = ChatRequestOptions()
    ): AppResult<LlmAnswer>

    /**
     * Отправляет в LLM полную историю диалога (system prompt + сообщения с ролями),
     * чтобы агент «помнил» контекст беседы, а не только последнюю реплику.
     */
    suspend fun sendConversation(
        messages: List<ChatMessage>,
        options: ChatRequestOptions = ChatRequestOptions()
    ): AppResult<LlmAnswer>

    suspend fun sendOpenRouterConversation(
        messages: List<ChatMessage>,
        modelId: String,
        options: ChatRequestOptions = ChatRequestOptions()
    ): AppResult<LlmAnswer>
}
