package com.example.feature.chat.presentation.format.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.UiState
import com.example.core.common.fold
import com.example.core.domain.usecase.SendChatMessageUseCase
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
class FormatViewModel @Inject constructor(
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val requestFactory: ChatRequestFactory,
    private val errorMessageMapper: ChatErrorMessageMapper
) : ViewModel() {
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    private val _selectedMode = MutableStateFlow(ResponseMode.Regular)
    val selectedMode: StateFlow<ResponseMode> = _selectedMode.asStateFlow()

    private val _comparisonState = MutableStateFlow(ChatComparisonState())
    val comparisonState: StateFlow<ChatComparisonState> = _comparisonState.asStateFlow()

    fun onMessageChanged(message: String) {
        _message.update { message }
    }

    fun onParametersEnabledChanged(isEnabled: Boolean) {
        _selectedMode.value = if (isEnabled) {
            ResponseMode.Structured
        } else {
            ResponseMode.Regular
        }
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

            sendChatMessageUseCase(
                message = request.apiPrompt,
                options = request.options
            ).fold(
                onSuccess = { response ->
                    updateComparisonState(
                        sourcePrompt = request.sourcePrompt,
                        mode = mode,
                        response = response
                    )
                    _uiState.value = UiState.Success(data = response)
                },
                onError = { error ->
                    _uiState.value = UiState.Error(errorMessageMapper.map(error))
                }
            )
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
