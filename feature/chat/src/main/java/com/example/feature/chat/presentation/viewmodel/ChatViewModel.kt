package com.example.feature.chat.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.UiState
import com.example.feature.chat.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onMessageChanged(message: String) {
        _message.update { message }
    }

    fun sendMessage() {
        val currentMessage = message.value.trim()
        if (currentMessage.isEmpty()) {
            _uiState.value = UiState.Error("Enter a message before sending.")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            runCatching {
                sendMessageUseCase(currentMessage)
            }.onSuccess { response ->
                Log.i("HTTP",response)
                _uiState.value = UiState.Success(response = response)
            }.onFailure { throwable ->
                _uiState.value = UiState.Error(
                    message = throwable.message ?: "Unexpected error. Try again later."
                )
            }
        }
    }
}
