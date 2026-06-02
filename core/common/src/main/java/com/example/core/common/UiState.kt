package com.example.core.common

sealed interface UiState {
    data object Idle : UiState

    data object Loading : UiState

    data class Success(
        val response: String
    ) : UiState

    data class Error(
        val message: String
    ) : UiState
}
