package com.example.feature.chat.presentation.temperature.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.chat.R
import com.example.feature.chat.presentation.temperature.model.TemperatureMode
import com.example.feature.chat.presentation.temperature.model.TemperatureResult
import com.example.feature.chat.presentation.temperature.model.TemperatureUiState
import com.example.feature.chat.presentation.temperature.viewmodel.TemperatureViewModel

@Composable
fun TemperatureRoute(viewModel: TemperatureViewModel) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    TemperatureScreen(
        uiState = uiState.value,
        onPromptChanged = viewModel::onPromptChanged,
        onRunClick = viewModel::runExperiment
    )
}

@Composable
fun TemperatureScreen(
    uiState: TemperatureUiState,
    onPromptChanged: (String) -> Unit,
    onRunClick: () -> Unit,
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
            text = stringResource(R.string.temperature_screen_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = uiState.prompt,
            onValueChange = onPromptChanged,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = stringResource(R.string.temperature_prompt_label))
            },
            placeholder = {
                Text(text = stringResource(R.string.temperature_prompt_placeholder))
            },
            minLines = 5,
            enabled = isBusy.not()
        )

        Button(
            onClick = onRunClick,
            modifier = Modifier.align(Alignment.End),
            enabled = isBusy.not()
        ) {
            Text(text = stringResource(R.string.temperature_run_button))
        }

        TemperatureStateContent(uiState = uiState)
    }
}

@Composable
private fun TemperatureStateContent(uiState: TemperatureUiState) {
    uiState.error?.let { error ->
        ErrorText(message = error)
    }

    if (uiState.results.isNotEmpty()) {
        TemperatureResultsContent(results = uiState.results)
    }

    if (uiState.isComparing) {
        LoadingCard(titleResId = R.string.temperature_conclusion_loading)
    }

    uiState.conclusion?.let { conclusion ->
        TextResultCard(
            titleResId = R.string.temperature_conclusion_title,
            text = conclusion
        )
    }
}

@Composable
private fun TemperatureResultsContent(
    results: Map<TemperatureMode, TemperatureResult>
) {
    TemperatureMode.entries.forEach { mode ->
        results[mode]?.let { result ->
            TemperatureResultCard(result = result)
        }
    }
}

@Composable
private fun TemperatureResultCard(result: TemperatureResult) {
    var isExpanded by rememberSaveable(result.mode.name) {
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = result.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

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
                    ErrorText(message = result.error)
                }

                result.answer.isNotBlank() -> {
                    Text(
                        text = result.answer,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = if (isExpanded) Int.MAX_VALUE else ANSWER_PREVIEW_LINES,
                        overflow = TextOverflow.Ellipsis
                    )
                    Button(
                        onClick = {
                            isExpanded = isExpanded.not()
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(
                                if (isExpanded) {
                                    R.string.temperature_collapse_answer
                                } else {
                                    R.string.temperature_expand_answer
                                }
                            )
                        )
                    }
                }

                else -> {
                    Text(
                        text = stringResource(R.string.temperature_waiting_for_answer),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private const val ANSWER_PREVIEW_LINES = 3

@Composable
private fun LoadingCard(@StringRes titleResId: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(titleResId),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun TextResultCard(
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
                style = MaterialTheme.typography.titleMedium,
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
private fun ErrorText(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Preview(showBackground = true)
@Composable
private fun TemperatureScreenPreview() {
    MaterialTheme {
        TemperatureScreen(
            uiState = TemperatureUiState(
                prompt = stringResource(R.string.preview_temperature_prompt),
                results = mapOf(
                    TemperatureMode.Precise to TemperatureResult(
                        mode = TemperatureMode.Precise,
                        title = stringResource(R.string.temperature_precise_title),
                        label = stringResource(R.string.temperature_precise_label),
                        answer = stringResource(R.string.preview_temperature_precise_answer)
                    ),
                    TemperatureMode.Balanced to TemperatureResult(
                        mode = TemperatureMode.Balanced,
                        title = stringResource(R.string.temperature_balanced_title),
                        label = stringResource(R.string.temperature_balanced_label),
                        answer = stringResource(R.string.preview_temperature_balanced_answer)
                    )
                ),
                conclusion = stringResource(R.string.preview_temperature_conclusion)
            ),
            onPromptChanged = {},
            onRunClick = {}
        )
    }
}
