package com.example.feature.chat.presentation.screen

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.example.feature.chat.presentation.model.ChatComparisonState
import com.example.feature.chat.presentation.model.ResponseMode
import com.example.feature.chat.presentation.viewmodel.ChatViewModel

@Composable
fun ChatRoute(
    viewModel: ChatViewModel
) {
    val message = viewModel.message.collectAsStateWithLifecycle()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val selectedMode = viewModel.selectedMode.collectAsStateWithLifecycle()
    val comparisonState = viewModel.comparisonState.collectAsStateWithLifecycle()

    ChatScreen(
        message = message.value,
        uiState = uiState.value,
        selectedMode = selectedMode.value,
        comparisonState = comparisonState.value,
        onMessageChanged = viewModel::onMessageChanged,
        onModeChanged = viewModel::onModeChanged,
        onSendClick = viewModel::sendMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    message: String,
    uiState: UiState,
    selectedMode: ResponseMode,
    comparisonState: ChatComparisonState,
    onMessageChanged: (String) -> Unit,
    onModeChanged: (ResponseMode) -> Unit,
    onSendClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.chat_screen_title))
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            ModeSelector(
                selectedMode = selectedMode,
                onModeChanged = onModeChanged,
                enabled = uiState !is UiState.Loading
            )

            Button(
                onClick = onSendClick,
                modifier = Modifier.align(Alignment.End),
                enabled = uiState !is UiState.Loading
            ) {
                Text(text = stringResource(R.string.send_button))
            }

            ChatStateContent(uiState = uiState)

            ComparisonContent(comparisonState = comparisonState)
        }
    }
}

@Composable
private fun ModeSelector(
    selectedMode: ResponseMode,
    onModeChanged: (ResponseMode) -> Unit,
    enabled: Boolean
) {
    val isSmartFormatEnabled = selectedMode == ResponseMode.Structured

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = isSmartFormatEnabled,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = { isEnabled ->
                    onModeChanged(
                        if (isEnabled) {
                            ResponseMode.Structured
                        } else {
                            ResponseMode.Regular
                        }
                    )
                }
            )
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.smart_format_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(selectedMode.labelResId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = isSmartFormatEnabled,
            onCheckedChange = null,
            enabled = enabled
        )
    }
}

@Composable
private fun ChatStateContent(
    uiState: UiState
) {
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
                text = uiState.response,
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
private fun ComparisonContent(
    comparisonState: ChatComparisonState
) {
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

    comparisonState.lastMode?.let { mode ->
        Text(
            text = stringResource(
                R.string.mode_value,
                stringResource(mode.labelResId)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }

    ResponseBlock(
        titleResId = R.string.unrestricted_response_title,
        response = comparisonState.regularResponse
    )

    ResponseBlock(
        titleResId = R.string.restricted_response_title,
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
private fun ChatScreenPreview() {
    MaterialTheme {
        ChatScreen(
            message = stringResource(R.string.preview_prompt),
            uiState = UiState.Success(stringResource(R.string.preview_last_response)),
            selectedMode = ResponseMode.Regular,
            comparisonState = ChatComparisonState(
                sourcePrompt = stringResource(R.string.preview_prompt),
                regularResponse = stringResource(R.string.preview_unrestricted_response),
                structuredResponse = stringResource(R.string.preview_restricted_response)
            ),
            onMessageChanged = {},
            onModeChanged = {},
            onSendClick = {}
        )
    }
}
