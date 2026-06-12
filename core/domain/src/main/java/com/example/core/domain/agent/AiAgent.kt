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
import com.example.core.model.ai.effectiveContextLimit
import javax.inject.Inject

private const val ROLE_USER = "user"
private const val ROLE_ASSISTANT = "assistant"

class AiAgent @Inject constructor(
    private val repository: AiChatRepository
) {
    /**
     * Принимает всю историю диалога (последнее сообщение должно быть от пользователя),
     * собирает system prompt из конфигурации агента и отправляет историю в LLM,
     * чтобы агент учитывал контекст беседы.
     *
     * Перед отправкой считает токены и проверяет лимит контекстного окна:
     *  - если влезает — шлём как есть;
     *  - если нет и включена авто-обрезка — отбрасываем самые старые сообщения (sliding window);
     *  - если нет и авто-обрезка выключена — возвращаем [AppError.ContextOverflow] (демонстрация
     *    того, что ломается при переполнении).
     */
    suspend fun ask(
        config: AgentConfig,
        conversation: List<AgentChatMessage>
    ): AppResult<AgentAnswer> {
        val lastMessage = conversation.lastOrNull()
        if (lastMessage == null ||
            lastMessage.author != AgentMessageAuthor.User ||
            lastMessage.text.isBlank()
        ) {
            return AppResult.Error(AppError.EmptyPrompt)
        }

        val systemPrompt = systemPromptOf(config)
        val prepared = prepareWithinLimit(
            systemPrompt = systemPrompt,
            conversation = conversation,
            limit = config.effectiveContextLimit(),
            autoTrim = config.autoTrimHistory
        ) ?: return AppResult.Error(AppError.ContextOverflow)

        val options = config.toRequestOptions(systemPrompt)
        val messages = prepared.map { it.toChatMessage() }

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
     * Подгоняет историю под лимит окна. Возвращает список сообщений для отправки или
     * `null`, если запрос не помещается (и авто-обрезка не помогла / выключена).
     */
    private fun prepareWithinLimit(
        systemPrompt: String,
        conversation: List<AgentChatMessage>,
        limit: Int,
        autoTrim: Boolean
    ): List<AgentChatMessage>? {
        val fullEstimate = TokenEstimator.estimateConversation(
            systemPrompt = systemPrompt,
            messageTexts = conversation.map { it.text }
        )
        if (fullEstimate <= limit) return conversation
        if (!autoTrim) return null

        // Sliding window: всегда сохраняем последнее (текущее) сообщение пользователя,
        // отбрасываем самые старые, пока не уложимся в лимит.
        val last = conversation.last()
        val minimalEstimate = TokenEstimator.estimateConversation(systemPrompt, listOf(last.text))
        if (minimalEstimate > limit) return null

        var start = 0
        while (start < conversation.lastIndex) {
            val window = conversation.subList(start, conversation.size)
            val estimate = TokenEstimator.estimateConversation(systemPrompt, window.map { it.text })
            if (estimate <= limit) return window
            start++
        }
        return listOf(last)
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
            systemPrompt = systemPrompt
        )

    /** System prompt запроса: роль агента + тема диалога + общая инструкция. */
    fun systemPromptOf(config: AgentConfig): String =
        buildString {
            append(config.systemPrompt.trim())
            if (config.dialogTheme.isNotBlank()) {
                append("\n\nТема текущего диалога: ")
                append(config.dialogTheme.trim())
            }
            append("\n\nТы отвечаешь как агент \"")
            append(config.name.trim())
            append("\". Держи ответы практичными, точными и полезными для пользователя.")
        }
}
