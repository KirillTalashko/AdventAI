package com.example.feature.chat.domain.model

data class ChatRequestOptions(
    val temperature: Double? = null,
    val maxTokens: Int? = null,
    val stop: List<String> = emptyList(),
    val thinkingMode: ThinkingMode? = null,
    val systemPrompt: String? = null
)

enum class ThinkingMode {
    Disabled
}
