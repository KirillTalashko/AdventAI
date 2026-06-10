package com.example.feature.chat.presentation.modelcomparison.model

import com.example.core.model.ai.TokenUsage

data class ModelComparisonResult(
    val tier: ModelTier,
    val title: String,
    val label: String,
    val docsUrl: String,
    val answer: String = "",
    val usage: TokenUsage? = null,
    val elapsedMillis: Long? = null,
    val estimatedCostUsd: Double? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
