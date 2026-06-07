package com.example.core.model.ai

data class LlmAnswer(
    val content: String,
    val reasoningContent: String? = null,
    val usage: TokenUsage? = null
)

data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val promptCacheHitTokens: Int = 0,
    val promptCacheMissTokens: Int = promptTokens
)
