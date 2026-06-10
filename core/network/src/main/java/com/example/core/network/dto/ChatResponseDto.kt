package com.example.core.network.dto

import com.google.gson.annotations.SerializedName

data class ChatResponseDto(
    @SerializedName("choices")
    val choices: List<ChoiceDto>,
    @SerializedName("usage")
    val usage: UsageDto?
)

data class UsageDto(
    @SerializedName("prompt_tokens")
    val promptTokens: Int?,
    @SerializedName("completion_tokens")
    val completionTokens: Int?,
    @SerializedName("total_tokens")
    val totalTokens: Int?,
    @SerializedName("prompt_cache_hit_tokens")
    val promptCacheHitTokens: Int?,
    @SerializedName("prompt_cache_miss_tokens")
    val promptCacheMissTokens: Int?
)
