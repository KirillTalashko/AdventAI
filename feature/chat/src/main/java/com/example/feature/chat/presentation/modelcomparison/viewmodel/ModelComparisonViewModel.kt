package com.example.feature.chat.presentation.modelcomparison.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.chat.R
import com.example.feature.chat.presentation.modelcomparison.model.ModelComparisonUiState
import com.example.feature.chat.presentation.modelcomparison.runner.ModelComparisonExperimentRunner
import com.example.feature.chat.presentation.text.ChatTextProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModelComparisonViewModel @Inject constructor(
    private val experimentRunner: ModelComparisonExperimentRunner,
    private val textProvider: ChatTextProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(ModelComparisonUiState())
    val uiState: StateFlow<ModelComparisonUiState> = _uiState.asStateFlow()

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
                state.copy(error = textProvider.get(R.string.error_enter_model_prompt))
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
