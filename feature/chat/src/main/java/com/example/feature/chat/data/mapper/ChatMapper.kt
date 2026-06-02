package com.example.feature.chat.data.mapper

import com.example.core.network.dto.ChatRequestDto
import com.example.core.network.dto.ChatResponseDto
import com.example.core.network.dto.MessageDto
import com.example.feature.chat.domain.exception.ChatException
import com.example.feature.chat.domain.model.ChatMessage

private const val DEEPSEEK_MODEL = "deepseek-v4-flash"
private const val USER_ROLE = "user"

fun String.toUserChatMessage(): ChatMessage =
    ChatMessage(role = USER_ROLE, content = this)

fun ChatMessage.toMessageDto(): MessageDto =
    MessageDto(role = role, content = content)

fun ChatMessage.toChatRequestDto(): ChatRequestDto =
    ChatRequestDto(
        model = DEEPSEEK_MODEL,
        messages = listOf(toMessageDto())
    )

fun ChatResponseDto.toResponseContent(): String =
    choices.firstOrNull()
        ?.message
        ?.content
        ?.takeIf(String::isNotBlank)
        ?: throw ChatException("DeepSeek returned an empty response.")
