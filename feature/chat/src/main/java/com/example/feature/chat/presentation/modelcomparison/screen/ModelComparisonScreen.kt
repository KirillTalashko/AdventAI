package com.example.feature.chat.presentation.modelcomparison.screen

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
import com.example.core.model.ai.TokenUsage
import com.example.feature.chat.R
import com.example.feature.chat.presentation.modelcomparison.model.ModelComparisonResult
import com.example.feature.chat.presentation.modelcomparison.model.ModelComparisonUiState
import com.example.feature.chat.presentation.modelcomparison.model.ModelTier
import com.example.feature.chat.presentation.modelcomparison.viewmodel.ModelComparisonViewModel

@Composable
fun ModelComparisonRoute(viewModel: ModelComparisonViewModel) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    ModelComparisonScreen(
        uiState = uiState.value,
        onPromptChanged = viewModel::onPromptChanged,
        onRunClick = viewModel::runExperiment
    )
}

@Composable
fun ModelComparisonScreen(
    uiState: ModelComparisonUiState,
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
            text = stringResource(R.string.model_screen_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = uiState.prompt,
            onValueChange = onPromptChanged,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = stringResource(R.string.model_prompt_label))
            },
            placeholder = {
                Text(text = stringResource(R.string.model_prompt_placeholder))
            },
            minLines = 5,
            enabled = isBusy.not()
        )

        Button(
            onClick = onRunClick,
            modifier = Modifier.align(Alignment.End),
            enabled = isBusy.not()
        ) {
            Text(text = stringResource(R.string.model_run_button))
        }

        ModelStateContent(uiState = uiState)
    }
}

@Composable
private fun ModelStateContent(uiState: ModelComparisonUiState) {
    uiState.error?.let { error ->
        ErrorText(message = error)
    }

    if (uiState.results.isNotEmpty()) {
        ModelResultsContent(results = uiState.results)
    }

    if (uiState.isComparing) {
        LoadingCard(titleResId = R.string.model_conclusion_loading)
    }

    uiState.conclusion?.let { conclusion ->
        TextResultCard(
            titleResId = R.string.model_conclusion_title,
            text = conclusion
        )
    }
}

@Composable
private fun ModelResultsContent(results: Map<ModelTier, ModelComparisonResult>) {
    ModelTier.entries.forEach { tier ->
        results[tier]?.let { result ->
            ModelResultCard(result = result)
        }
    }
}

@Composable
private fun ModelResultCard(result: ModelComparisonResult) {
    var isExpanded by rememberSaveable(result.tier.name) {
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

            ModelMetrics(result = result)

            Text(
                text = stringResource(R.string.model_docs_label, result.docsUrl),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    R.string.model_collapse_answer
                                } else {
                                    R.string.model_expand_answer
                                }
                            )
                        )
                    }
                }

                else -> {
                    Text(
                        text = stringResource(R.string.model_waiting_for_answer),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelMetrics(result: ModelComparisonResult) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = result.elapsedMillis?.let { millis ->
                stringResource(R.string.model_metric_time, millis)
            } ?: stringResource(R.string.model_metric_time_unknown),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = result.usage?.let { usage ->
                usage.toMetricText()
            } ?: stringResource(R.string.model_metric_tokens_unknown),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = result.estimatedCostUsd?.let { cost ->
                stringResource(R.string.model_metric_cost, cost)
            } ?: stringResource(R.string.model_metric_cost_unknown),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun TokenUsage.toMetricText(): String =
    stringResource(
        R.string.model_metric_tokens,
        promptTokens,
        completionTokens,
        totalTokens
    )

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

private const val ANSWER_PREVIEW_LINES = 3

@Preview(showBackground = true)
@Composable
private fun ModelComparisonScreenPreview() {
    MaterialTheme {
        ModelComparisonScreen(
            uiState = ModelComparisonUiState(
                prompt = stringResource(R.string.preview_model_prompt),
                results = mapOf(
                    ModelTier.DeepSeekBaseline to ModelComparisonResult(
                        tier = ModelTier.DeepSeekBaseline,
                        title = stringResource(R.string.model_deepseek_title),
                        label = stringResource(R.string.model_deepseek_label),
                        docsUrl = stringResource(R.string.deepseek_pricing_url),
                        answer = stringResource(R.string.preview_model_deepseek_answer),
                        usage = TokenUsage(
                            promptTokens = 40,
                            completionTokens = 120,
                            totalTokens = 160
                        ),
                        elapsedMillis = 900,
                        estimatedCostUsd = 0.000039
                    )
                ),
                conclusion = stringResource(R.string.preview_model_conclusion)
            ),
            onPromptChanged = {},
            onRunClick = {}
        )
    }
}
