package com.example.feature.chat.presentation.compression.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.chat.presentation.compression.model.CompressionAbUiState
import com.example.feature.chat.presentation.compression.runner.CompressionAbRunner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompressionAbViewModel @Inject constructor(
    private val runner: CompressionAbRunner
) : ViewModel() {
    private val _uiState = MutableStateFlow(CompressionAbUiState())
    val uiState: StateFlow<CompressionAbUiState> = _uiState.asStateFlow()

    private var runJob: Job? = null

    fun runExperiment() {
        if (uiState.value.isRunning) return
        _uiState.value = CompressionAbUiState(isRunning = true)
        runJob = viewModelScope.launch {
            runner.run().collect { state -> _uiState.value = state }
        }
    }

    fun onStop() {
        runJob?.cancel()
        runJob = null
        _uiState.value = _uiState.value.copy(isRunning = false, activeVariant = null)
    }

    override fun onCleared() {
        super.onCleared()
        runJob?.cancel()
    }
}
