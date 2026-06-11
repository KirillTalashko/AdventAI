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
     * Принимает всю историю диалога (последнее сообщение должно быть от пользователя),
     * собирает system prompt из конфигурации агента и отправляет историю в LLM,
     * чтобы агент учитывал контекст беседы.
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

        val options = config.toRequestOptions()
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
                    modelTitle = config.model.title
                )
            )

            is AppResult.Error -> answer
        }
    }

    private fun AgentChatMessage.toChatMessage(): ChatMessage =
        ChatMessage(
            role = when (author) {
                AgentMessageAuthor.User -> ROLE_USER
                AgentMessageAuthor.Agent -> ROLE_ASSISTANT
            },
            content = text
        )

    private fun AgentConfig.toRequestOptions(): ChatRequestOptions =
        ChatRequestOptions(
            model = model.deepSeekModel ?: ChatRequestOptions().model,
            systemPrompt = buildSystemPrompt()
        )

    private fun AgentConfig.buildSystemPrompt(): String =
        buildString {
            append(systemPrompt.trim())
            if (dialogTheme.isNotBlank()) {
                append("\n\nТема текущего диалога: ")
                append(dialogTheme.trim())
            }
            append("\n\nТы отвечаешь как агент \"")
            append(name.trim())
            append("\". Держи ответы практичными, точными и полезными для пользователя.")
        }
}
