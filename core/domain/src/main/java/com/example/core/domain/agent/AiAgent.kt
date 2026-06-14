package com.example.core.domain.agent

import com.example.core.common.AppError
import com.example.core.common.AppResult
import com.example.core.domain.repository.AiChatRepository
import com.example.core.model.ai.AgentAnswer
import com.example.core.model.ai.AgentChatMessage
import com.example.core.model.ai.AgentConfig
import com.example.core.model.ai.AgentMessageAuthor
import com.example.core.model.ai.AgentProvider
import com.example.core.model.ai.ChatMessage
import com.example.core.model.ai.ChatRequestOptions
import javax.inject.Inject

private const val ROLE_USER = "user"
private const val ROLE_ASSISTANT = "assistant"

class AiAgent @Inject constructor(
    private val repository: AiChatRepository
) {
    /**
     * Принимает всю историю диалога (последнее сообщение — от пользователя), собирает
     * system prompt и отправляет историю целиком в LLM (агент помнит контекст).
     *
     * Контекстное окно (Day 8): историю НЕ обрезаем — sliding window будет в следующих заданиях.
     *  - если задан демо-лимит окна и оценка его превышает → [AppError.ContextOverflow]
     *    (детерминированная демонстрация переполнения на коротком диалоге);
     *  - иначе шлём всю историю; если она превысит реальное окно модели, переполнение
     *    вернёт сам API (мапится в [AppError.ContextOverflow] в data-слое).
     */
    suspend fun ask(
        config: AgentConfig,
        conversation: List<AgentChatMessage>,
        memory: String? = null
    ): AppResult<AgentAnswer> {
        val lastMessage = conversation.lastOrNull()
        if (lastMessage == null ||
            lastMessage.author != AgentMessageAuthor.User ||
            lastMessage.text.isBlank()
        ) {
            return AppResult.Error(AppError.EmptyPrompt)
        }

        val systemPrompt = systemPromptOf(config, memory)

        config.demoContextLimitTokens?.let { demoLimit ->
            val estimate = TokenEstimator.estimateConversation(systemPrompt, conversation.map { it.text })
            if (estimate > demoLimit) {
                return AppResult.Error(AppError.ContextOverflow)
            }
        }

        val options = config.toRequestOptions(systemPrompt)
        val messages = conversation.map { it.toChatMessage() }

        val answer = when (config.model.provider) {
            AgentProvider.DeepSeek -> repository.sendConversation(
                messages = messages,
                options = options
            )

            AgentProvider.OpenRouter -> repository.sendOpenRouterConversation(
                messages = messages,
                modelId = config.model.apiId,
                options = options
            )
        }

        return when (answer) {
            is AppResult.Success -> AppResult.Success(
                AgentAnswer(
                    content = answer.data.content,
                    modelTitle = config.model.title,
                    usage = answer.data.usage
                )
            )

            is AppResult.Error -> answer
        }
    }

    /**
     * Оценка токенов контекста (system prompt + история + черновик) для индикатора
     * заполнения окна в UI. Считается локально, до отправки запроса.
     */
    fun estimateContextTokens(
        config: AgentConfig,
        conversation: List<AgentChatMessage>,
        draft: String = ""
    ): Int {
        val texts = conversation.map { it.text } +
            if (draft.isNotBlank()) listOf(draft) else emptyList()
        return TokenEstimator.estimateConversation(systemPromptOf(config), texts)
    }

    /**
     * Оценка токенов **сжатого** контекста (Day 9): system prompt + summary + только сырой хвост
     * (без свёрнутых старых сообщений). Нужна, чтобы показать экономию «сжато vs полная история».
     */
    fun estimateCompressedContextTokens(
        config: AgentConfig,
        recent: List<AgentChatMessage>,
        memory: String?,
        draft: String = ""
    ): Int {
        val texts = recent.map { it.text } +
            if (draft.isNotBlank()) listOf(draft) else emptyList()
        return TokenEstimator.estimateConversation(systemPromptOf(config, memory), texts)
    }

    private fun AgentChatMessage.toChatMessage(): ChatMessage =
        ChatMessage(
            role = when (author) {
                AgentMessageAuthor.User -> ROLE_USER
                AgentMessageAuthor.Agent -> ROLE_ASSISTANT
            },
            content = text
        )

    private fun AgentConfig.toRequestOptions(systemPrompt: String): ChatRequestOptions =
        ChatRequestOptions(
            model = model.deepSeekModel ?: ChatRequestOptions().model,
            systemPrompt = systemPrompt,
            temperature = temperature,
            topP = topP,
            maxTokens = maxTokens,
            thinkingMode = thinkingMode
        )

    /**
     * System prompt запроса: роль агента + тема диалога + общая инструкция.
     * Если задан [memory] (сжатая память Day 9), он добавляется отдельным блоком — модель
     * учитывает факты из summary, хотя самих старых сообщений в запросе уже нет.
     */
    fun systemPromptOf(config: AgentConfig, memory: String? = null): String =
        buildString {
            append(config.systemPrompt.trim())
            if (config.dialogTheme.isNotBlank()) {
                append("\n\nТема текущего диалога: ")
                append(config.dialogTheme.trim())
            }
            append("\n\nТы отвечаешь как агент \"")
            append(config.name.trim())
            append("\". Держи ответы практичными, точными и полезными для пользователя.")
            if (!memory.isNullOrBlank()) {
                append("\n\nСжатая память предыдущей части диалога (опирайся на эти факты, ")
                append("самих ранних сообщений в запросе уже нет):\n")
                append(memory.trim())
            }
        }
}
