package com.example.feature.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.UiState
import com.example.core.common.fold
import com.example.core.domain.usecase.SendChatMessageUseCase
import com.example.feature.chat.presentation.mapper.ChatErrorMessageMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val errorMessageMapper: ChatErrorMessageMapper
) : ViewModel() {
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    fun onMessageChanged(message: String) {
        _message.update { message }
    }

    fun sendMessage() {
        val prompt = message.value.trim()

        if (prompt.isBlank()) {
            _uiState.value = UiState.Error(errorMessageMapper.emptyMessage())
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            sendChatMessageUseCase(message = prompt).fold(
                onSuccess = { response ->
                    _uiState.value = UiState.Success(data = response)
                },
                onError = { error ->
                    _uiState.value = UiState.Error(errorMessageMapper.map(error))
                }
            )
        }
    }
}
