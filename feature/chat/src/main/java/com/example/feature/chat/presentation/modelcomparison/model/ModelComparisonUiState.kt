package com.example.feature.chat.presentation.modelcomparison.model

data class ModelComparisonUiState(
    val prompt: String = "",
    val results: Map<ModelTier, ModelComparisonResult> = emptyMap(),
    val conclusion: String? = null,
    val isLoadingAnswers: Boolean = false,
    val isComparing: Boolean = false,
    val error: String? = null
)
