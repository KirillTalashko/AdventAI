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
     * Сколько самых старых сообщений уже «выехало» из окна — для подсветки в ленте.
     * Возвращает индекс первого сообщения, которое ещё попадёт в запрос (с учётом
     * текущего черновика). `0` — все сообщения помещаются. Считается только когда
     * включена авто-обрезка (иначе обрезки нет, переполнение даёт ошибку).
     */
    fun windowStartIndex(
        config: AgentConfig,
        conversation: List<AgentChatMessage>,
        draft: String = ""
    ): Int {
        if (!config.autoTrimHistory || conversation.isEmpty()) return 0
        val systemPrompt = systemPromptOf(config)
        val limit = config.effectiveContextLimit()
        val effective = if (draft.isNotBlank()) {
            conversation + AgentChatMessage(author = AgentMessageAuthor.User, text = draft)
        } else {
            conversation
        }
        val start = windowStart(systemPrompt, effective, limit) ?: return conversation.size
        return start.coerceAtMost(conversation.size)
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
        val start = windowStart(systemPrompt, conversation, limit)
        return when {
            start == 0 -> conversation        // всё помещается
            !autoTrim -> null                 // переполнение без авто-обрезки → ошибка
            start == null -> null             // не лезет даже одно последнее сообщение
            else -> conversation.subList(start, conversation.size)
        }
    }

    /**
     * Индекс первого сообщения, которое помещается в окно при sliding window.
     * `0` — всё помещается; `null` — не помещается даже одно последнее сообщение.
     * Всегда сохраняет последнее сообщение, отбрасывая самые старые.
     */
    private fun windowStart(
        systemPrompt: String,
        conversation: List<AgentChatMessage>,
        limit: Int
    ): Int? {
        if (conversation.isEmpty()) return 0
        val full = TokenEstimator.estimateConversation(systemPrompt, conversation.map { it.text })
        if (full <= limit) return 0

        val last = conversation.last()
        if (TokenEstimator.estimateConversation(systemPrompt, listOf(last.text)) > limit) return null

        var start = 0
        while (start < conversation.lastIndex) {
            val window = conversation.subList(start, conversation.size)
            if (TokenEstimator.estimateConversation(systemPrompt, window.map { it.text }) <= limit) {
                return start
            }
            start++
        }
        return conversation.lastIndex
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
