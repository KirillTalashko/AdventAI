package com.example.feature.chat.presentation.reasoning.model

data class ReasoningUiState(
    val task: String = "",
    val selectedModes: Set<ReasoningMode> = emptySet(),
    val results: Map<ReasoningMode, ReasoningResult> = emptyMap(),
    val comparison: String? = null,
    val isDeepReasoningEnabled: Boolean = false,
    val isLoadingAnswers: Boolean = false,
    val isComparing: Boolean = false,
    val error: String? = null
)
