package com.example.feature.chat.presentation.temperature.model

data class TemperatureResult(
    val mode: TemperatureMode,
    val title: String,
    val label: String,
    val answer: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
