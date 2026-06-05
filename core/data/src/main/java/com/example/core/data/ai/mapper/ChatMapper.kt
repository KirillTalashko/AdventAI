package com.example.core.data.ai.mapper

import com.example.core.common.AppError
import com.example.core.model.ai.ChatMessage
import com.example.core.model.ai.ChatRequestOptions
import com.example.core.model.ai.DeepSeekModel
import com.example.core.model.ai.LlmAnswer
import com.example.core.model.ai.ThinkingMode
import com.example.core.network.dto.ChatRequestDto
import com.example.core.network.dto.ChatResponseDto
import com.example.core.network.dto.MessageDto
import com.example.core.network.dto.ThinkingDto

private const val SYSTEM_ROLE = "system"
private const val USER_ROLE = "user"
private const val THINKING_DISABLED = "disabled"
private const val FINISH_REASON_LENGTH = "length"

fun String.toUserChatMessage(): ChatMessage =
    ChatMessage(role = USER_ROLE, content = this)

fun ChatMessage.toChatRequestDto(options: ChatRequestOptions): ChatRequestDto =
    ChatRequestDto(
        model = options.model.apiName,
        messages = options.toMessageDtos(userMessage = this),
        thinking = options.thinkingMode.takeIf { options.model == DeepSeekModel.Fast }?.toThinkingDto(),
        temperature = options.temperature.takeIf { options.model == DeepSeekModel.Fast },
        maxTokens = options.maxTokens,
        stop = options.stop.takeIf { it.isNotEmpty() }
    )

fun ChatResponseDto.toLlmAnswerOrError(): Either<AppError, LlmAnswer> {
    val choice = choices.firstOrNull() ?: return Either.Left(AppError.EmptyResponse)
    val message = choice.message
    val content = message?.content?.takeIf(String::isNotBlank)

    return when {
        content != null -> Either.Right(
            LlmAnswer(
                content = content,
                reasoningContent = message?.reasoningContent?.takeIf(String::isNotBlank)
            )
        )

        choice.finishReason == FINISH_REASON_LENGTH -> Either.Left(AppError.TokenLimitReached)
        else -> Either.Left(AppError.EmptyResponse)
    }
}

private fun ChatMessage.toMessageDto(): MessageDto =
    MessageDto(role = role, content = content)

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

sealed interface Either<out L, out R> {
    data class Left<L>(val value: L) : Either<L, Nothing>
    data class Right<R>(val value: R) : Either<Nothing, R>
}
