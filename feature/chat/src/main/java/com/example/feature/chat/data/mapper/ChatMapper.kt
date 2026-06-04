package com.example.feature.chat.data.mapper

import com.example.core.network.dto.ChatRequestDto
import com.example.core.network.dto.ChatResponseDto
import com.example.core.network.dto.MessageDto
import com.example.core.network.dto.ThinkingDto
import com.example.feature.chat.domain.exception.ChatException
import com.example.feature.chat.domain.model.ChatMessage
import com.example.feature.chat.domain.model.ChatRequestOptions
import com.example.feature.chat.domain.model.ThinkingMode

private const val DEEPSEEK_MODEL = "deepseek-v4-flash"
private const val SYSTEM_ROLE = "system"
private const val USER_ROLE = "user"
private const val THINKING_DISABLED = "disabled"
private const val FINISH_REASON_LENGTH = "length"

fun String.toUserChatMessage(): ChatMessage =
    ChatMessage(role = USER_ROLE, content = this)

fun ChatMessage.toMessageDto(): MessageDto =
    MessageDto(role = role, content = content)

fun ChatMessage.toChatRequestDto(options: ChatRequestOptions): ChatRequestDto =
    ChatRequestDto(
        model = DEEPSEEK_MODEL,
        messages = options.toMessageDtos(userMessage = this),
        thinking = options.thinkingMode?.toThinkingDto(),
        temperature = options.temperature,
        maxTokens = options.maxTokens,
        stop = options.stop.takeIf { it.isNotEmpty() }
    )

fun ChatResponseDto.toResponseContent(): String {
    val choice = choices.firstOrNull() ?: throw ChatException.EmptyResponse()
    val content = choice.message?.content?.takeIf(String::isNotBlank)

    return when {
        content != null -> content
        choice.finishReason == FINISH_REASON_LENGTH -> throw ChatException.TokenLimitReached()
        else -> throw ChatException.EmptyResponse()
    }
}

private fun ThinkingMode.toThinkingDto(): ThinkingDto = when (this) {
    ThinkingMode.Disabled -> ThinkingDto(type = THINKING_DISABLED)
}

private fun ChatRequestOptions.toMessageDtos(userMessage: ChatMessage): List<MessageDto> =
    buildList {
        systemPrompt
            ?.takeIf(String::isNotBlank)
            ?.let { prompt ->
                add(MessageDto(role = SYSTEM_ROLE, content = prompt))
            }
        add(userMessage.toMessageDto())
    }
