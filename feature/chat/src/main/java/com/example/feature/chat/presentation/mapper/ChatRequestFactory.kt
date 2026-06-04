package com.example.feature.chat.presentation.mapper

import com.example.feature.chat.R
import com.example.feature.chat.domain.model.ChatRequestOptions
import com.example.feature.chat.domain.model.ThinkingMode
import com.example.feature.chat.presentation.model.PreparedChatRequest
import com.example.feature.chat.presentation.model.ResponseMode
import com.example.feature.chat.presentation.text.ChatTextProvider
import javax.inject.Inject

class ChatRequestFactory @Inject constructor(
    private val textProvider: ChatTextProvider
) {
    fun create(
        sourcePrompt: String,
        mode: ResponseMode
    ): PreparedChatRequest {
        val trimmedPrompt = sourcePrompt.trim()

        return when (mode) {
            ResponseMode.Regular -> PreparedChatRequest(
                sourcePrompt = trimmedPrompt,
                apiPrompt = trimmedPrompt,
                options = ChatRequestOptions()
            )

            ResponseMode.Structured -> PreparedChatRequest(
                sourcePrompt = trimmedPrompt,
                apiPrompt = textProvider.get(
                    R.string.restricted_prompt_template,
                    trimmedPrompt
                ),
                options = ChatRequestOptions(
                    temperature = STRUCTURED_TEMPERATURE,
                    maxTokens = STRUCTURED_MAX_TOKENS,
                    stop = listOf(textProvider.get(R.string.stop_sequence_end_response)),
                    thinkingMode = ThinkingMode.Disabled,
                    systemPrompt = textProvider.get(R.string.structured_system_prompt)
                )
            )
        }
    }

    private companion object {
        const val STRUCTURED_TEMPERATURE = 0.3
        const val STRUCTURED_MAX_TOKENS = 350
    }
}
