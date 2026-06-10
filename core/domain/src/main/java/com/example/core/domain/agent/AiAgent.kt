package com.example.core.domain.agent

import com.example.core.common.AppError
import com.example.core.common.AppResult
import com.example.core.domain.repository.AiChatRepository
import com.example.core.model.ai.AgentAnswer
import com.example.core.model.ai.AgentConfig
import com.example.core.model.ai.AgentProvider
import com.example.core.model.ai.ChatRequestOptions
import javax.inject.Inject

class AiAgent @Inject constructor(
    private val repository: AiChatRepository
) {
    suspend fun ask(
        config: AgentConfig,
        userRequest: String
    ): AppResult<AgentAnswer> {
        val prompt = userRequest.trim()
        if (prompt.isBlank()) {
            return AppResult.Error(AppError.EmptyPrompt)
        }

        val options = config.toRequestOptions()
        val answer = when (config.model.provider) {
            AgentProvider.DeepSeek -> repository.sendDetailedMessage(
                message = prompt,
                options = options
            )

            AgentProvider.OpenRouter -> repository.sendOpenRouterDetailedMessage(
                message = prompt,
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
