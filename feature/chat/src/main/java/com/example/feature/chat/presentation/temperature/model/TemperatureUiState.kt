package com.example.feature.chat.presentation.temperature.model

data class TemperatureUiState(
    val prompt: String = "",
    val results: Map<TemperatureMode, TemperatureResult> = emptyMap(),
    val conclusion: String? = null,
    val isLoadingAnswers: Boolean = false,
    val isComparing: Boolean = false,
    val error: String? = null
)
