package com.example.feature.chat.presentation.format.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.common.UiState
import com.example.feature.chat.R
import com.example.feature.chat.presentation.format.viewmodel.FormatViewModel
import com.example.feature.chat.presentation.model.ChatComparisonState
import com.example.feature.chat.presentation.model.ResponseMode

@Composable
fun FormatRoute(viewModel: FormatViewModel) {
    val message = viewModel.message.collectAsStateWithLifecycle()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val selectedMode = viewModel.selectedMode.collectAsStateWithLifecycle()
    val comparisonState = viewModel.comparisonState.collectAsStateWithLifecycle()

    FormatScreen(
        message = message.value,
        uiState = uiState.value,
        isParametersEnabled = selectedMode.value == ResponseMode.Structured,
        comparisonState = comparisonState.value,
        onMessageChanged = viewModel::onMessageChanged,
        onParametersEnabledChanged = viewModel::onParametersEnabledChanged,
        onSendClick = viewModel::sendMessage
    )
}

@Composable
fun FormatScreen(
    message: String,
    uiState: UiState<String>,
    isParametersEnabled: Boolean,
    comparisonState: ChatComparisonState,
    onMessageChanged: (String) -> Unit,
    onParametersEnabledChanged: (Boolean) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.format_screen_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = message,
            onValueChange = onMessageChanged,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = stringResource(R.string.message_label))
            },
            minLines = 4,
            enabled = uiState !is UiState.Loading
        )

        ParametersSwitch(
            isChecked = isParametersEnabled,
            onCheckedChange = onParametersEnabledChanged,
            enabled = uiState !is UiState.Loading
        )

        Button(
            onClick = onSendClick,
            modifier = Modifier.align(Alignment.End),
            enabled = uiState !is UiState.Loading
        ) {
            Text(text = stringResource(R.string.send_button))
        }

        FormatStateContent(uiState = uiState)

        FormatComparisonContent(comparisonState = comparisonState)
    }
}

@Composable
private fun ParametersSwitch(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = isChecked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.parameters_switch_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        Switch(
            checked = isChecked,
            onCheckedChange = null,
            enabled = enabled
        )
    }
}

@Composable
private fun FormatStateContent(uiState: UiState<String>) {
    when (uiState) {
        UiState.Idle -> Unit
        UiState.Loading -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is UiState.Success -> {
            Text(
                text = stringResource(R.string.last_response_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = uiState.data,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        is UiState.Error -> {
            Text(
                text = uiState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun FormatComparisonContent(comparisonState: ChatComparisonState) {
    if (comparisonState.sourcePrompt.isBlank()) {
        return
    }

    Text(
        text = stringResource(R.string.source_prompt_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Text(
        text = comparisonState.sourcePrompt,
        style = MaterialTheme.typography.bodyLarge
    )

    ResponseBlock(
        titleResId = R.string.response_without_parameters_title,
        response = comparisonState.regularResponse
    )

    ResponseBlock(
        titleResId = R.string.response_with_parameters_title,
        response = comparisonState.structuredResponse
    )
}

@Composable
private fun ResponseBlock(
    @StringRes titleResId: Int,
    response: String?
) {
    Text(
        text = stringResource(titleResId),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
    Text(
        text = response ?: stringResource(R.string.response_not_received),
        style = MaterialTheme.typography.bodyLarge,
        color = if (response == null) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun FormatScreenPreview() {
    MaterialTheme {
        FormatScreen(
            message = stringResource(R.string.preview_prompt),
            uiState = UiState.Success(data = stringResource(R.string.preview_last_response)),
            isParametersEnabled = true,
            comparisonState = ChatComparisonState(
                sourcePrompt = stringResource(R.string.preview_prompt),
                regularResponse = stringResource(R.string.preview_unrestricted_response),
                structuredResponse = stringResource(R.string.preview_restricted_response)
            ),
            onMessageChanged = {},
            onParametersEnabledChanged = {},
            onSendClick = {}
        )
    }
}
