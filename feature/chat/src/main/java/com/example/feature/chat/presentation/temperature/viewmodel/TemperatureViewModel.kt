package com.example.feature.chat.presentation.temperature.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.chat.R
import com.example.feature.chat.presentation.temperature.model.TemperatureUiState
import com.example.feature.chat.presentation.temperature.runner.TemperatureExperimentRunner
import com.example.feature.chat.presentation.text.ChatTextProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemperatureViewModel @Inject constructor(
    private val experimentRunner: TemperatureExperimentRunner,
    private val textProvider: ChatTextProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(TemperatureUiState())
    val uiState: StateFlow<TemperatureUiState> = _uiState.asStateFlow()

    fun onPromptChanged(prompt: String) {
        _uiState.update { state ->
            state.copy(
                prompt = prompt,
                results = emptyMap(),
                conclusion = null,
                error = null
            )
        }
    }

    fun runExperiment() {
        val prompt = uiState.value.prompt.trim()

        if (prompt.isBlank()) {
            _uiState.update { state ->
                state.copy(error = textProvider.get(R.string.error_enter_temperature_prompt))
            }
            return
        }

        viewModelScope.launch {
            experimentRunner.run(prompt).collect { state ->
                _uiState.value = state
            }
        }
    }
}
