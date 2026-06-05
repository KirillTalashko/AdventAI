package com.example.feature.chat.presentation.reasoning.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.chat.R
import com.example.feature.chat.presentation.reasoning.model.ReasoningMode
import com.example.feature.chat.presentation.reasoning.model.ReasoningResult
import com.example.feature.chat.presentation.reasoning.model.ReasoningUiState
import com.example.feature.chat.presentation.reasoning.viewmodel.ReasoningViewModel

@Composable
fun ReasoningRoute(viewModel: ReasoningViewModel) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    ReasoningScreen(
        uiState = uiState.value,
        onTaskChanged = viewModel::onTaskChanged,
        onModeToggled = viewModel::onModeToggled,
        onDeepReasoningChanged = viewModel::onDeepReasoningChanged,
        onRunAnswersClick = viewModel::runSelectedModes,
        onCompareClick = viewModel::compareResults
    )
}

@Composable
fun ReasoningScreen(
    uiState: ReasoningUiState,
    onTaskChanged: (String) -> Unit,
    onModeToggled: (ReasoningMode) -> Unit,
    onDeepReasoningChanged: (Boolean) -> Unit,
    onRunAnswersClick: () -> Unit,
    onCompareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBusy = uiState.isLoadingAnswers || uiState.isComparing

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.reasoning_screen_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        TaskCard(
            task = uiState.task,
            isEnabled = isBusy.not(),
            onTaskChanged = onTaskChanged
        )

        ReasoningModeCard(
            selectedModes = uiState.selectedModes,
            onModeToggled = onModeToggled,
            enabled = isBusy.not()
        )

        ReasoningOptionsCard(
            isDeepReasoningEnabled = uiState.isDeepReasoningEnabled,
            onDeepReasoningChanged = onDeepReasoningChanged,
            enabled = isBusy.not()
        )

        ActionButtons(
            onRunAnswersClick = onRunAnswersClick,
            onCompareClick = onCompareClick,
            enabled = isBusy.not()
        )

        ReasoningStateContent(uiState = uiState)
    }
}

@Composable
private fun TaskCard(
    task: String,
    isEnabled: Boolean,
    onTaskChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.reasoning_source_task_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = task,
                onValueChange = onTaskChanged,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = stringResource(R.string.reasoning_task_label))
                },
                placeholder = {
                    Text(text = stringResource(R.string.reasoning_task_placeholder))
                },
                minLines = 5,
                enabled = isEnabled
            )
        }
    }
}

@Composable
private fun ReasoningModeCard(
    selectedModes: Set<ReasoningMode>,
    onModeToggled: (ReasoningMode) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.reasoning_method_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReasoningMode.entries.forEach { mode ->
                    FilterChip(
                        selected = mode in selectedModes,
                        onClick = {
                            onModeToggled(mode)
                        },
                        enabled = enabled,
                        label = {
                            Text(text = stringResource(mode.chipTitleResId))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReasoningOptionsCard(
    isDeepReasoningEnabled: Boolean,
    onDeepReasoningChanged: (Boolean) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.reasoning_options_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            ReasoningSwitchRow(
                title = stringResource(R.string.reasoning_deep_switch),
                value = isDeepReasoningEnabled,
                onCheckedChange = onDeepReasoningChanged,
                enabled = enabled
            )
        }
    }
}

@Composable
private fun ReasoningSwitchRow(
    title: String,
    value: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = value,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Switch(
            checked = value,
            onCheckedChange = null,
            enabled = enabled
        )
    }
}

@Composable
private fun ActionButtons(
    onRunAnswersClick: () -> Unit,
    onCompareClick: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onRunAnswersClick,
            modifier = Modifier.weight(1f),
            enabled = enabled
        ) {
            Text(text = stringResource(R.string.reasoning_run_answers_button))
        }
        OutlinedButton(
            onClick = onCompareClick,
            modifier = Modifier.weight(1f),
            enabled = enabled
        ) {
            Text(text = stringResource(R.string.reasoning_compare_button))
        }
    }
}

@Composable
private fun ReasoningStateContent(uiState: ReasoningUiState) {
    if (uiState.isLoadingAnswers || uiState.isComparing) {
        LoadingContent()
    }

    uiState.error?.let { error ->
        ErrorContent(message = error)
    }

    if (uiState.results.isNotEmpty()) {
        ReasoningResultsContent(results = uiState.results)
    }

    uiState.comparison?.let { comparison ->
        ResultCard(
            titleResId = R.string.reasoning_comparison_title,
            text = comparison
        )
    }
}

@Composable
private fun LoadingContent() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun ReasoningResultsContent(
    results: Map<ReasoningMode, ReasoningResult>
) {
    ReasoningMode.entries.forEach { mode ->
        results[mode]?.let { result ->
            ReasoningResultCard(result = result)
        }
    }
}

@Composable
private fun ReasoningResultCard(result: ReasoningResult) {
    var isPromptVisible by rememberSaveable(result.mode.name, "prompt") {
        mutableStateOf(false)
    }
    var isReasoningVisible by rememberSaveable(result.mode.name) {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = result.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            when {
                result.isLoading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                result.error != null -> {
                    Text(
                        text = result.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                result.answer.isNotBlank() -> {
                    LabeledText(
                        titleResId = R.string.reasoning_answer_label,
                        text = result.answer
                    )

                    PromptToggle(
                        isPromptVisible = isPromptVisible,
                        onToggle = {
                            isPromptVisible = isPromptVisible.not()
                        }
                    )

                    if (isPromptVisible) {
                        LabeledText(
                            titleResId = R.string.reasoning_prompt_label,
                            text = result.prompt
                        )
                    }

                    result.reasoningContent?.let { reasoningContent ->
                        TextButton(
                            onClick = {
                                isReasoningVisible = isReasoningVisible.not()
                            }
                        ) {
                            Text(
                                text = stringResource(
                                    if (isReasoningVisible) {
                                        R.string.reasoning_hide_reasoning
                                    } else {
                                        R.string.reasoning_show_reasoning
                                    }
                                )
                            )
                        }

                        if (isReasoningVisible) {
                            LabeledText(
                                titleResId = R.string.reasoning_content_label,
                                text = reasoningContent
                            )
                        }
                    }
                }

                else -> {
                    Text(
                        text = stringResource(R.string.reasoning_waiting_for_answer),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (result.answer.isBlank() && result.prompt.isNotBlank()) {
                PromptToggle(
                    isPromptVisible = isPromptVisible,
                    onToggle = {
                        isPromptVisible = isPromptVisible.not()
                    }
                )

                if (isPromptVisible) {
                    LabeledText(
                        titleResId = R.string.reasoning_prompt_label,
                        text = result.prompt
                    )
                }
            }
        }
    }
}

@Composable
private fun PromptToggle(
    isPromptVisible: Boolean,
    onToggle: () -> Unit
) {
    TextButton(onClick = onToggle) {
        Text(
            text = stringResource(
                if (isPromptVisible) {
                    R.string.reasoning_hide_prompt
                } else {
                    R.string.reasoning_show_prompt
                }
            )
        )
    }
}

@Composable
private fun ResultCard(
    @StringRes titleResId: Int,
    text: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(titleResId),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = text,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun LabeledText(
    @StringRes titleResId: Int,
    text: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(titleResId),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReasoningScreenPreview() {
    MaterialTheme {
        ReasoningScreen(
            uiState = ReasoningUiState(
                task = stringResource(R.string.preview_reasoning_task),
                selectedModes = setOf(ReasoningMode.StepByStep),
                results = mapOf(
                    ReasoningMode.StepByStep to ReasoningResult(
                        mode = ReasoningMode.StepByStep,
                        title = stringResource(R.string.reasoning_step_by_step_answer_title),
                        prompt = stringResource(R.string.preview_reasoning_step_prompt),
                        answer = stringResource(R.string.preview_reasoning_step_answer)
                    )
                )
            ),
            onTaskChanged = {},
            onModeToggled = {},
            onDeepReasoningChanged = {},
            onRunAnswersClick = {},
            onCompareClick = {}
        )
    }
}
