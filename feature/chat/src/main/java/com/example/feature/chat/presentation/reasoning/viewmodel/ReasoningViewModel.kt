package com.example.feature.chat.presentation.reasoning.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.chat.R
import com.example.feature.chat.presentation.reasoning.model.ReasoningMode
import com.example.feature.chat.presentation.reasoning.model.ReasoningUiState
import com.example.feature.chat.presentation.reasoning.runner.ReasoningExperimentRunner
import com.example.feature.chat.presentation.text.ChatTextProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReasoningViewModel @Inject constructor(
    private val experimentRunner: ReasoningExperimentRunner,
    private val textProvider: ChatTextProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReasoningUiState())
    val uiState: StateFlow<ReasoningUiState> = _uiState.asStateFlow()

    fun onTaskChanged(task: String) {
        _uiState.update { currentState ->
            currentState.copy(
                task = task,
                results = emptyMap(),
                comparison = null,
                error = null
            )
        }
    }

    fun onModeToggled(mode: ReasoningMode) {
        _uiState.update { currentState ->
            val updatedModes = if (mode in currentState.selectedModes) {
                currentState.selectedModes - mode
            } else {
                currentState.selectedModes + mode
            }

            currentState.copy(
                selectedModes = updatedModes,
                results = emptyMap(),
                comparison = null,
                error = null
            )
        }
    }

    fun onDeepReasoningChanged(isEnabled: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isDeepReasoningEnabled = isEnabled,
                results = emptyMap(),
                comparison = null,
                error = null
            )
        }
    }

    fun runSelectedModes() {
        val currentState = uiState.value
        val task = currentState.task.trim()

        if (validateTask(task).not() || validateSelectedModes(currentState).not()) {
            return
        }

        viewModelScope.launch {
            experimentRunner.runAnswers(
                task = task,
                selectedModes = currentState.selectedModes,
                isDeepReasoningEnabled = currentState.isDeepReasoningEnabled
            ).collect { reasoningState ->
                _uiState.value = reasoningState
            }
        }
    }

    fun compareResults() {
        val currentState = uiState.value
        val task = currentState.task.trim()
        val receivedResults = currentState.results.filterValues { result ->
            result.answer.isNotBlank()
        }

        if (validateTask(task).not()) {
            return
        }

        if (receivedResults.isEmpty()) {
            _uiState.update { state ->
                state.copy(error = textProvider.get(R.string.error_no_reasoning_answers))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isComparing = true,
                    comparison = null,
                    error = null
                )
            }

            runCatching {
                experimentRunner.compareResults(
                    task = task,
                    results = receivedResults,
                    isDeepReasoningEnabled = currentState.isDeepReasoningEnabled
                )
            }.onSuccess { comparison ->
                _uiState.update { state ->
                    state.copy(
                        comparison = comparison,
                        isComparing = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isComparing = false,
                        error = throwable.message ?: textProvider.get(R.string.error_unexpected)
                    )
                }
            }
        }
    }

    private fun validateTask(task: String): Boolean {
        if (task.isNotBlank()) {
            return true
        }

        _uiState.update { state ->
            state.copy(error = textProvider.get(R.string.error_enter_reasoning_task))
        }
        return false
    }

    private fun validateSelectedModes(state: ReasoningUiState): Boolean {
        if (state.selectedModes.isNotEmpty()) {
            return true
        }

        _uiState.update { currentState ->
            currentState.copy(error = textProvider.get(R.string.error_select_reasoning_mode))
        }
        return false
    }
}
