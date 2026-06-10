package com.example.feature.chat.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.ai.AgentChatMessage
import com.example.core.model.ai.AgentConfig
import com.example.core.model.ai.AgentLlmModel
import com.example.core.model.ai.AgentMessageAuthor
import com.example.core.model.ai.AgentProvider
import com.example.feature.chat.R
import com.example.feature.chat.presentation.viewmodel.AgentChatUiState
import com.example.feature.chat.presentation.viewmodel.ChatViewModel

@Composable
fun ChatRoute(
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    ChatScreen(
        state = uiState.value,
        onNavigateBack = onNavigateBack,
        onMessageChanged = viewModel::onMessageChanged,
        onSendClick = viewModel::sendMessage,
        onAgentNameChanged = viewModel::onAgentNameChanged,
        onModelSelected = viewModel::onModelSelected,
        onSystemPromptChanged = viewModel::onSystemPromptChanged,
        onDialogThemeChanged = viewModel::onDialogThemeChanged
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    state: AgentChatUiState,
    onNavigateBack: () -> Unit,
    onMessageChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    onAgentNameChanged: (String) -> Unit,
    onModelSelected: (AgentLlmModel) -> Unit,
    onSystemPromptChanged: (String) -> Unit,
    onDialogThemeChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettings by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    val visibleMessages = state.messages

    LaunchedEffect(visibleMessages.size, state.isLoading) {
        val lastIndex = visibleMessages.lastIndex
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AgentHeader(
                config = state.config,
                onNavigateBack = onNavigateBack,
                onSettingsClick = { showSettings = true }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .imeNestedScroll(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visibleMessages) { message ->
                    MessageBubble(message = message)
                }
                if (state.isLoading) {
                    item {
                        LoadingBubble()
                    }
                }
                state.errorMessage?.let { message ->
                    item {
                        ErrorMessage(message = message)
                    }
                }
            }

            MessageComposer(
                value = state.message,
                isLoading = state.isLoading,
                onValueChange = onMessageChanged,
                onSendClick = onSendClick,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        }

        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 10.dp,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 6.dp)
                            .size(width = 44.dp, height = 5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            ) {
                AgentSettingsSheet(
                    state = state,
                    onAgentNameChanged = onAgentNameChanged,
                    onModelSelected = onModelSelected,
                    onSystemPromptChanged = onSystemPromptChanged,
                    onDialogThemeChanged = onDialogThemeChanged,
                    onClose = { showSettings = false }
                )
            }
        }
    }
}

@Composable
private fun AgentHeader(
    config: AgentConfig,
    onNavigateBack: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 12.dp, top = 2.dp, end = 12.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.22f),
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopBarIconButton(
                iconRes = R.drawable.ic_arrow_back_24,
                contentDescription = "Назад",
                onClick = onNavigateBack
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = config.name.ifBlank { "AI-агент" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = config.dialogTheme.ifBlank { "Готов к диалогу" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            TopBarIconButton(
                iconRes = R.drawable.ic_settings_24,
                contentDescription = "Настройки",
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun TopBarIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White.copy(alpha = 0.16f),
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(containerColor)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = contentColor
        )
    }
}

@Composable
private fun MessageBubble(
    message: AgentChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.author == AgentMessageAuthor.User
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 330.dp),
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp,
                bottomStart = if (isUser) 24.dp else 8.dp,
                bottomEnd = if (isUser) 8.dp else 24.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isUser) 0.dp else 1.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun LoadingBubble(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Агент отвечает...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun MessageComposer(
    value: String,
    isLoading: Boolean,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 4.dp),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                minLines = 1,
                maxLines = 4,
                enabled = isLoading.not(),
                placeholder = {
                    Text(text = "Напишите запрос агенту")
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (isLoading.not() && value.isNotBlank()) {
                            onSendClick()
                        }
                    }
                )
            )
            SendIconButton(
                enabled = isLoading.not() && value.isNotBlank(),
                onClick = onSendClick
            )
        }
    }
}

@Composable
private fun SendIconButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val foreground = if (enabled) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        SendGlyph(color = foreground)
    }
}

@Composable
private fun AgentSettingsSheet(
    state: AgentChatUiState,
    onAgentNameChanged: (String) -> Unit,
    onModelSelected: (AgentLlmModel) -> Unit,
    onSystemPromptChanged: (String) -> Unit,
    onDialogThemeChanged: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Настройки агента",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Роль, модель и контекст ответа",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TopBarIconButton(
                iconRes = R.drawable.ic_close_24,
                contentDescription = "Закрыть",
                onClick = onClose,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }

        SettingsSection {
            SoftTextField(
                value = state.config.name,
                onValueChange = onAgentNameChanged,
                label = "Имя агента",
                singleLine = true
            )
            SoftTextField(
                value = state.config.dialogTheme,
                onValueChange = onDialogThemeChanged,
                label = "Тема диалога",
                minLines = 2
            )
        }

        SettingsSection(
            title = "Модель для API"
        ) {
            ModelSelector(
                selectedModel = state.config.model,
                models = state.availableModels,
                onModelSelected = onModelSelected
            )
        }

        SettingsSection {
            SoftTextField(
                value = state.config.systemPrompt,
                onValueChange = onSystemPromptChanged,
                label = "Системный промпт",
                minLines = 6
            )
        }

        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 4.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Сохранить дизайн агента",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SettingsSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
private fun SoftTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    singleLine: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            singleLine = singleLine,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun ModelSelector(
    selectedModel: AgentLlmModel,
    models: List<AgentLlmModel>,
    onModelSelected: (AgentLlmModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        models.forEach { model ->
            ModelOptionCard(
                model = model,
                selected = model == selectedModel,
                onClick = { onModelSelected(model) }
            )
        }
    }
}

@Composable
private fun ModelOptionCard(
    model: AgentLlmModel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        tonalElevation = if (selected) 3.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModelSelectionDot(
                selected = selected,
                color = contentColor
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = model.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = model.provider.label(),
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.72f)
                )
            }
        }
    }
}

@Composable
private fun SendGlyph(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.7.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, size.height * 0.5f),
            end = Offset(size.width * 0.78f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.58f, size.height * 0.28f),
            end = Offset(size.width * 0.80f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.58f, size.height * 0.72f),
            end = Offset(size.width * 0.80f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun ModelSelectionDot(
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(22.dp)) {
        drawCircle(
            color = color.copy(alpha = if (selected) 1f else 0.36f),
            style = Stroke(width = 2.dp.toPx())
        )
        if (selected) {
            drawCircle(
                color = color,
                radius = size.minDimension * 0.24f
            )
        }
    }
}

private fun AgentProvider.label(): String = when (this) {
    AgentProvider.DeepSeek -> "DeepSeek API"
    AgentProvider.OpenRouter -> "OpenRouter API"
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    MaterialTheme {
        ChatScreen(
            state = AgentChatUiState(
                config = AgentConfig(
                    name = "Визовый специалист",
                    model = AgentLlmModel.DeepSeekFlash,
                    systemPrompt = "Ты визовый специалист.",
                    dialogTheme = "Подготовка к подаче"
                ),
                message = "",
                messages = listOf(
                    AgentChatMessage(
                        author = AgentMessageAuthor.Agent,
                        text = "Здравствуйте. Опишите страну, тип визы и вашу ситуацию."
                    ),
                    AgentChatMessage(
                        author = AgentMessageAuthor.User,
                        text = "Какие документы нужны для туристической визы?"
                    )
                )
            ),
            onNavigateBack = {},
            onMessageChanged = {},
            onSendClick = {},
            onAgentNameChanged = {},
            onModelSelected = {},
            onSystemPromptChanged = {},
            onDialogThemeChanged = {}
        )
    }
}
