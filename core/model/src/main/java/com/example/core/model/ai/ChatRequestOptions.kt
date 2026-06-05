package com.example.core.model.ai

data class ChatRequestOptions(
    val model: DeepSeekModel = DeepSeekModel.Fast,
    val temperature: Double? = null,
    val maxTokens: Int? = null,
    val stop: List<String> = emptyList(),
    val thinkingMode: ThinkingMode? = null,
    val systemPrompt: String? = null
)

enum class DeepSeekModel(
    val apiName: String
) {
    Fast("deepseek-v4-flash"),
    Reasoner("deepseek-reasoner")
}

enum class ThinkingMode {
    Disabled
}
