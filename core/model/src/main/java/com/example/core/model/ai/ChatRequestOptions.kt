package com.example.core.model.ai

data class ChatRequestOptions(
    val model: DeepSeekModel = DeepSeekModel.Fast,
    val temperature: Double? = null,
    val topP: Double? = null,
    val maxTokens: Int? = null,
    val stop: List<String> = emptyList(),
    val thinkingMode: ThinkingMode? = null,
    val systemPrompt: String? = null
)

enum class DeepSeekModel(
    val apiName: String,
    val supportsThinkingParameter: Boolean = false,
    val supportsTemperature: Boolean = false
) {
    Fast(
        apiName = "deepseek-v4-flash",
        supportsThinkingParameter = true,
        supportsTemperature = true
    ),
    Pro(
        apiName = "deepseek-v4-pro",
        supportsThinkingParameter = true,
        supportsTemperature = true
    ),
    Reasoner(apiName = "deepseek-reasoner")
}

enum class ThinkingMode {
    Disabled
}
