package com.example.feature.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.UiState
import com.example.feature.chat.domain.usecase.SendMessageUseCase
import com.example.feature.chat.presentation.mapper.ChatErrorMessageMapper
import com.example.feature.chat.presentation.mapper.ChatRequestFactory
import com.example.feature.chat.presentation.model.ChatComparisonState
import com.example.feature.chat.presentation.model.ResponseMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val requestFactory: ChatRequestFactory,
    private val errorMessageMapper: ChatErrorMessageMapper
) : ViewModel() {
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _selectedMode = MutableStateFlow(ResponseMode.Regular)
    val selectedMode: StateFlow<ResponseMode> = _selectedMode.asStateFlow()

    private val _comparisonState = MutableStateFlow(ChatComparisonState())
    val comparisonState: StateFlow<ChatComparisonState> = _comparisonState.asStateFlow()

    fun onMessageChanged(message: String) {
        _message.update { message }
    }

    fun onModeChanged(mode: ResponseMode) {
        _selectedMode.value = mode
    }

    fun sendMessage() {
        if (message.value.isBlank()) {
            _uiState.value = UiState.Error(errorMessageMapper.emptyMessage())
            return
        }

        val mode = selectedMode.value
        val request = requestFactory.create(
            sourcePrompt = message.value,
            mode = mode
        )

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            runCatching {
                sendMessageUseCase(
                    message = request.apiPrompt,
                    options = request.options
                )
            }.onSuccess { response ->
                updateComparisonState(
                    sourcePrompt = request.sourcePrompt,
                    mode = mode,
                    response = response
                )
                _uiState.value = UiState.Success(response = response)
            }.onFailure { throwable ->
                _uiState.value = UiState.Error(errorMessageMapper.map(throwable))
            }
        }
    }

    private fun updateComparisonState(
        sourcePrompt: String,
        mode: ResponseMode,
        response: String
    ) {
        _comparisonState.update { currentState ->
            val baseState = if (currentState.sourcePrompt == sourcePrompt) {
                currentState
            } else {
                ChatComparisonState(sourcePrompt = sourcePrompt)
            }

            when (mode) {
                ResponseMode.Regular -> baseState.copy(
                    regularResponse = response,
                    lastMode = mode
                )

                ResponseMode.Structured -> baseState.copy(
                    structuredResponse = response,
                    lastMode = mode
                )
            }
        }
    }
}
