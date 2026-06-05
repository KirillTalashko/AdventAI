package com.example.feature.chat.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.common.UiState
import com.example.feature.chat.R
import com.example.feature.chat.presentation.viewmodel.ChatViewModel

@Composable
fun ChatRoute(viewModel: ChatViewModel) {
    val message = viewModel.message.collectAsStateWithLifecycle()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    ChatScreen(
        message = message.value,
        uiState = uiState.value,
        onMessageChanged = viewModel::onMessageChanged,
        onSendClick = viewModel::sendMessage
    )
}

@Composable
fun ChatScreen(
    message: String,
    uiState: UiState<String>,
    onMessageChanged: (String) -> Unit,
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
            text = stringResource(R.string.chat_screen_title),
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

        Button(
            onClick = onSendClick,
            modifier = Modifier.align(Alignment.End),
            enabled = uiState !is UiState.Loading
        ) {
            Text(text = stringResource(R.string.send_button))
        }

        ChatStateContent(uiState = uiState)
    }
}

@Composable
private fun ChatStateContent(uiState: UiState<String>) {
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

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    MaterialTheme {
        ChatScreen(
            message = stringResource(R.string.preview_prompt),
            uiState = UiState.Success(data = stringResource(R.string.preview_last_response)),
            onMessageChanged = {},
            onSendClick = {}
        )
    }
}
