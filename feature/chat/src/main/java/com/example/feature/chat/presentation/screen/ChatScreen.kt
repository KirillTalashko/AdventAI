package com.example.feature.chat.presentation.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.designsystem.component.DocumentChecklistCard
import com.example.core.designsystem.component.ProgressRing
import com.example.core.designsystem.theme.AdventAITheme
import com.example.core.designsystem.theme.AppRadii
import com.example.core.designsystem.theme.AppSpacing
import com.example.core.designsystem.theme.AdventTheme
import com.example.core.model.ai.AgentChatMessage
import com.example.core.model.ai.AgentConfig
import com.example.core.model.ai.AgentLlmModel
import com.example.core.model.ai.AgentMessageAuthor
import com.example.core.model.ai.AgentProvider
import com.example.core.model.ai.Conversation
import com.example.feature.chat.R
import com.example.feature.chat.presentation.viewmodel.AgentChatUiState
import com.example.feature.chat.presentation.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
        onStopClick = viewModel::onStopGeneration,
        onNewDialog = viewModel::onNewDialog,
        onSelectConversation = viewModel::onSelectConversation,
        onDeleteConversation = viewModel::onDeleteConversation,
        onAgentNameChanged = viewModel::onAgentNameChanged,
        onModelSelected = viewModel::onModelSelected,
        onSystemPromptChanged = viewModel::onSystemPromptChanged,
        onDialogThemeChanged = viewModel::onDialogThemeChanged,
        onDemoContextLimitSelected = viewModel::onDemoContextLimitSelected,
        onAutoTrimChanged = viewModel::onAutoTrimChanged
    )
}

private val SuggestedPrompts = listOf(
    "Какие документы нужны?",
    "Сроки подачи",
    "Риски отказа",
    "Проверь мой пакет"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    state: AgentChatUiState,
    onNavigateBack: () -> Unit,
    onMessageChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit,
    onNewDialog: () -> Unit,
    onSelectConversation: (Long) -> Unit,
    onDeleteConversation: (Long) -> Unit,
    onAgentNameChanged: (String) -> Unit,
    onModelSelected: (AgentLlmModel) -> Unit,
    onSystemPromptChanged: (String) -> Unit,
    onDialogThemeChanged: (String) -> Unit,
    onDemoContextLimitSelected: (Int?) -> Unit,
    onAutoTrimChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettings by remember { mutableStateOf(false) }
    var showConversations by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val conversationsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val messages = state.messages
    val isEmptyConversation = messages.none { it.author == AgentMessageAuthor.User }
    val chatItems = remember(messages, state.windowStartIndex) {
        buildChatItems(messages, state.windowStartIndex)
    }
    val scrollAnchor = chatItems.size + (if (state.isLoading) 1 else 0)

    LaunchedEffect(scrollAnchor) {
        val target = (scrollAnchor - 1).coerceAtLeast(0)
        if (scrollAnchor > 0) {
            listState.animateScrollToItem(target)
        }
    }

    fun submit(text: String) {
        onMessageChanged(text)
        onSendClick()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AgentHeader(
                config = state.config,
                isTyping = state.isLoading,
                onNavigateBack = onNavigateBack,
                onConversationsClick = { showConversations = true },
                onSettingsClick = { showSettings = true }
            )

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (isEmptyConversation) {
                    EmptyChatState(
                        config = state.config,
                        greeting = messages.lastOrNull { it.author == AgentMessageAuthor.Agent }?.text,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    MessageList(
                        listState = listState,
                        chatItems = chatItems,
                        isLoading = state.isLoading,
                        errorMessage = state.errorMessage
                    )
                    val showScrollDown by remember {
                        derivedStateOf { listState.canScrollForward }
                    }
                    if (showScrollDown) {
                        ScrollToBottomButton(
                            onClick = {
                                scope.launch {
                                    listState.animateScrollToItem(
                                        (scrollAnchor - 1).coerceAtLeast(0)
                                    )
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = AppSpacing.lg, bottom = AppSpacing.sm)
                        )
                    }
                }
            }

            if (isEmptyConversation) {
                SuggestionRow(
                    prompts = SuggestedPrompts,
                    enabled = !state.isLoading,
                    onClick = ::submit
                )
            }

            ContextMeter(
                used = state.contextTokens,
                limit = state.contextLimit,
                autoTrim = state.config.autoTrimHistory,
                messagesInWindow = state.messages.size - state.windowStartIndex,
                messagesTotal = state.messages.size
            )

            MessageComposer(
                value = state.message,
                isLoading = state.isLoading,
                onValueChange = onMessageChanged,
                onSendClick = onSendClick,
                onStopClick = onStopClick,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        }

        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = AppRadii.sheet, topEnd = AppRadii.sheet),
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { SheetHandle() }
            ) {
                AgentSettingsSheet(
                    state = state,
                    onAgentNameChanged = onAgentNameChanged,
                    onModelSelected = onModelSelected,
                    onSystemPromptChanged = onSystemPromptChanged,
                    onDialogThemeChanged = onDialogThemeChanged,
                    onDemoContextLimitSelected = onDemoContextLimitSelected,
                    onAutoTrimChanged = onAutoTrimChanged,
                    onClose = { showSettings = false }
                )
            }
        }

        if (showConversations) {
            ModalBottomSheet(
                onDismissRequest = { showConversations = false },
                sheetState = conversationsSheetState,
                shape = RoundedCornerShape(topStart = AppRadii.sheet, topEnd = AppRadii.sheet),
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { SheetHandle() }
            ) {
                ConversationsSheet(
                    conversations = state.conversations,
                    activeConversationId = state.activeConversationId,
                    onNewDialog = {
                        showConversations = false
                        onNewDialog()
                    },
                    onSelectConversation = { id ->
                        showConversations = false
                        onSelectConversation(id)
                    },
                    onDeleteConversation = onDeleteConversation,
                    onClose = { showConversations = false }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------

@Composable
private fun AgentHeader(
    config: AgentConfig,
    isTyping: Boolean,
    onNavigateBack: () -> Unit,
    onConversationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(
                iconRes = R.drawable.ic_arrow_back_24,
                contentDescription = "Назад",
                onClick = onNavigateBack
            )
            AgentAvatar(name = config.name, size = 40.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = config.name.ifBlank { "AI-агент" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                AgentStatusLine(isTyping = isTyping)
            }
            CircleIconButton(
                iconRes = R.drawable.ic_forum_24,
                contentDescription = "Диалоги",
                onClick = onConversationsClick
            )
            CircleIconButton(
                iconRes = R.drawable.ic_settings_24,
                contentDescription = "Настройки",
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun AgentStatusLine(
    isTyping: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isTyping) {
            TypingDots(color = MaterialTheme.colorScheme.primary, dotSize = 5.dp)
            Text(
                text = "печатает…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        } else {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(AdventTheme.status.success)
            )
            Text(
                text = "онлайн",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Message list
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MessageList(
    listState: androidx.compose.foundation.lazy.LazyListState,
    chatItems: List<ChatListItem>,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imeNestedScroll(),
        state = listState,
        contentPadding = PaddingValues(
            start = AppSpacing.lg,
            end = AppSpacing.lg,
            top = AppSpacing.md,
            bottom = AppSpacing.md
        ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        itemsIndexed(chatItems) { _, item ->
            when (item) {
                is ChatListItem.DateHeader -> DateSeparator(label = item.label)
                ChatListItem.WindowDivider -> WindowDivider()
                is ChatListItem.Bubble -> MessageBubble(
                    message = item.message,
                    isLastInGroup = item.isLastInGroup,
                    topSpacing = if (item.isFirstInGroup) AppSpacing.sm else 0.dp,
                    outOfWindow = item.outOfWindow
                )
            }
        }
        if (isLoading) {
            item { TypingBubble() }
        }
        errorMessage?.let { message ->
            item { ErrorMessage(message = message) }
        }
    }
}

@Composable
private fun DateSeparator(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = 5.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun WindowDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
        )
        Text(
            text = stringResource(R.string.window_divider_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
        )
    }
}

@Composable
private fun MessageBubble(
    message: AgentChatMessage,
    isLastInGroup: Boolean,
    topSpacing: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    outOfWindow: Boolean = false
) {
    val isUser = message.author == AgentMessageAuthor.User
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val tail = AppRadii.bubble
    val flat = 6.dp
    val shape = RoundedCornerShape(
        topStart = tail,
        topEnd = tail,
        bottomStart = if (isUser || !isLastInGroup) tail else flat,
        bottomEnd = if (!isUser || !isLastInGroup) tail else flat
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topSpacing),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (outOfWindow) {
            Text(
                text = stringResource(R.string.message_out_of_window),
                modifier = Modifier.padding(start = 6.dp, end = 6.dp, bottom = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        Surface(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .alpha(if (outOfWindow) 0.4f else 1f),
            shape = shape,
            color = bubbleColor
        ) {
            Column(
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                if (isUser) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor
                    )
                } else {
                    parseAgentMessage(message.text).forEach { part ->
                        when (part) {
                            is AgentMessagePart.Text -> Text(
                                text = part.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = contentColor
                            )

                            is AgentMessagePart.Checklist -> DocumentChecklistCard(
                                items = part.items,
                                title = "Пакет документов"
                            )
                        }
                    }
                }
            }
        }
        if (!isUser) {
            message.usage?.let { usage -> MessageTokenInfo(usage = usage) }
        }
        if (isLastInGroup) {
            val time = formatTime(message.createdAt)
            if (time.isNotEmpty()) {
                Text(
                    text = time,
                    modifier = Modifier.padding(top = 3.dp, start = 6.dp, end = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MessageTokenInfo(
    usage: com.example.core.model.ai.TokenUsage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = 3.dp, start = 6.dp, end = 6.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            text = stringResource(
                R.string.tokens_message_usage,
                usage.promptTokens,
                usage.completionTokens,
                usage.totalTokens
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (usage.promptCacheHitTokens > 0) {
            Text(
                text = stringResource(R.string.tokens_message_cache, usage.promptCacheHitTokens),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ContextMeter(
    used: Int,
    limit: Int,
    autoTrim: Boolean,
    messagesInWindow: Int,
    messagesTotal: Int,
    modifier: Modifier = Modifier
) {
    if (limit <= 0) return
    val fraction = (used.toFloat() / limit.toFloat()).coerceIn(0f, 1f)
    val overflow = used > limit
    val warning = !overflow && fraction >= 0.8f
    val barColor = when {
        overflow -> MaterialTheme.colorScheme.error
        warning -> AdventTheme.status.warning
        else -> MaterialTheme.colorScheme.primary
    }
    val statusText = when {
        overflow && autoTrim -> stringResource(R.string.context_meter_overflow_trim)
        overflow -> stringResource(R.string.context_meter_overflow_block)
        warning -> stringResource(R.string.context_meter_warning)
        else -> null
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    R.string.context_meter_label,
                    formatTokens(used),
                    formatTokens(limit)
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            statusText?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(start = AppSpacing.sm),
                    style = MaterialTheme.typography.labelSmall,
                    color = barColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(barColor)
            )
        }
        if (autoTrim && messagesTotal > 0) {
            Text(
                text = stringResource(
                    R.string.context_meter_messages,
                    messagesInWindow,
                    messagesTotal
                ),
                style = MaterialTheme.typography.labelSmall,
                color = if (messagesInWindow < messagesTotal) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1
            )
        }
    }
}

private fun formatTokens(value: Int): String =
    String.format(Locale.getDefault(), "%,d", value)

@Composable
private fun TypingBubble(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.sm),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = AppRadii.bubble,
                topEnd = AppRadii.bubble,
                bottomStart = 6.dp,
                bottomEnd = AppRadii.bubble
            ),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Box(
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.lg)
            ) {
                TypingDots(color = MaterialTheme.colorScheme.onSurfaceVariant, dotSize = 8.dp)
            }
        }
    }
}

@Composable
private fun TypingDots(
    color: Color,
    dotSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dotSize / 2)
    ) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, delayMillis = index * 160, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$index"
            )
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.sm),
        shape = RoundedCornerShape(AppRadii.bubble),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(AppSpacing.lg),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

// ---------------------------------------------------------------------------
// Empty / welcome state
// ---------------------------------------------------------------------------

@Composable
private fun EmptyChatState(
    config: AgentConfig,
    greeting: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
    ) {
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        AgentAvatar(name = config.name, size = 76.dp)
        Text(
            text = config.name.ifBlank { "AI-агент" },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (!greeting.isNullOrBlank()) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
        ReadinessCard(progress = 0.6f)
    }
}

@Composable
private fun ReadinessCard(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.cardLarge),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressRing(
                progress = progress,
                diameter = 84.dp,
                strokeWidth = 9.dp
            ) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                Text(
                    text = "Готовность к подаче",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Соберите документы и проверьте пакет — я подскажу следующий шаг.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestionRow(
    prompts: List<String>,
    enabled: Boolean,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        prompts.forEach { prompt ->
            SuggestionChip(text = prompt, enabled = enabled, onClick = { onClick(prompt) })
        }
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(CircleShape).clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------------------------------------------------------------------------
// Composer
// ---------------------------------------------------------------------------

@Composable
private fun MessageComposer(
    value: String,
    isLoading: Boolean,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canSend = value.isNotBlank() && !isLoading
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = AppSpacing.md, top = AppSpacing.sm, end = AppSpacing.md, bottom = AppSpacing.sm),
        shape = RoundedCornerShape(AppRadii.cardLarge),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(
                iconRes = R.drawable.ic_add_24,
                contentDescription = "Прикрепить документ",
                onClick = { /* TODO: вложения (фото/скан документа) — отдельная фича */ },
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(max = 148.dp),
                minLines = 1,
                maxLines = 6,
                placeholder = { Text(text = "Сообщение агенту") },
                shape = RoundedCornerShape(AppRadii.field),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { if (canSend) onSendClick() }
                )
            )
            ComposerActionButton(
                isLoading = isLoading,
                canSend = canSend,
                onSend = onSendClick,
                onStop = onStopClick
            )
        }
    }
}

@Composable
private fun ComposerActionButton(
    isLoading: Boolean,
    canSend: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = isLoading || canSend
    val background = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val foreground = if (enabled) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled) { if (isLoading) onStop() else onSend() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(foreground)
            )
        } else {
            SendGlyph(color = foreground)
        }
    }
}

@Composable
private fun ScrollToBottomButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            ChevronDownGlyph(color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom sheets
// ---------------------------------------------------------------------------

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .padding(top = AppSpacing.md, bottom = AppSpacing.sm)
            .size(width = 44.dp, height = 5.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun ConversationsSheet(
    conversations: List<Conversation>,
    activeConversationId: Long?,
    onNewDialog: () -> Unit,
    onSelectConversation: (Long) -> Unit,
    onDeleteConversation: (Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        SheetHeader(
            title = "Диалоги",
            subtitle = "Темы с этим агентом",
            onClose = onClose
        )
        NewDialogButton(onClick = onNewDialog)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            contentPadding = PaddingValues(bottom = AppSpacing.md)
        ) {
            items(conversations, key = { it.id }) { conversation ->
                ConversationRow(
                    conversation = conversation,
                    selected = conversation.id == activeConversationId,
                    onClick = { onSelectConversation(conversation.id) },
                    onDelete = { onDeleteConversation(conversation.id) }
                )
            }
        }
    }
}

@Composable
private fun SheetHeader(
    title: String,
    subtitle: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        CircleIconButton(
            iconRes = R.drawable.ic_close_24,
            contentDescription = "Закрыть",
            onClick = onClose
        )
    }
}

@Composable
private fun NewDialogButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadii.card))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppRadii.card),
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add_24),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "Новый диалог",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadii.bubble))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppRadii.bubble),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }
    ) {
        Row(
            modifier = Modifier.padding(start = AppSpacing.lg, top = AppSpacing.sm, end = AppSpacing.sm, bottom = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = conversation.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatRelativeDay(conversation.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            CircleIconButton(
                iconRes = R.drawable.ic_delete_24,
                contentDescription = "Удалить диалог",
                onClick = onDelete,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AgentSettingsSheet(
    state: AgentChatUiState,
    onAgentNameChanged: (String) -> Unit,
    onModelSelected: (AgentLlmModel) -> Unit,
    onSystemPromptChanged: (String) -> Unit,
    onDialogThemeChanged: (String) -> Unit,
    onDemoContextLimitSelected: (Int?) -> Unit,
    onAutoTrimChanged: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
    ) {
        SheetHeader(
            title = "Настройки агента",
            subtitle = "Роль, модель и контекст ответа",
            onClose = onClose
        )

        SettingsSection(title = "Идентичность") {
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

        SettingsSection(title = "Модель") {
            ModelSelector(
                selectedModel = state.config.model,
                models = state.availableModels,
                onModelSelected = onModelSelected
            )
        }

        SettingsSection(title = "Поведение") {
            SoftTextField(
                value = state.config.systemPrompt,
                onValueChange = onSystemPromptChanged,
                label = "Системный промпт",
                minLines = 6
            )
        }

        SettingsSection(title = stringResource(R.string.settings_section_context)) {
            ContextLimitChips(
                selected = state.config.demoContextLimitTokens,
                onSelect = onDemoContextLimitSelected
            )
            AutoTrimRow(
                checked = state.config.autoTrimHistory,
                onCheckedChange = onAutoTrimChanged
            )
        }

        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(AppRadii.bubble),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Готово",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.xs))
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
        shape = RoundedCornerShape(AppRadii.card),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
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
            shape = RoundedCornerShape(AppRadii.bubble),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

private val ContextLimitPresets: List<Int?> = listOf(null, 4000, 2000, 1000, 500)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContextLimitChips(
    selected: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Text(
            text = stringResource(R.string.settings_context_limit_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            ContextLimitPresets.forEach { preset ->
                SelectableChip(
                    text = preset?.let { formatTokens(it) }
                        ?: stringResource(R.string.settings_context_limit_real),
                    selected = preset == selected,
                    onClick = { onSelect(preset) }
                )
            }
        }
    }
}

@Composable
private fun SelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(CircleShape).clickable(onClick = onClick),
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        border = if (selected) {
            null
        } else {
            androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AutoTrimRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_auto_trim_label),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.settings_auto_trim_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
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
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
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
            .clip(RoundedCornerShape(AppRadii.bubble))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppRadii.bubble),
        color = containerColor,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModelSelectionDot(selected = selected, color = contentColor)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = model.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = model.provider.label(),
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.72f),
                    maxLines = 1
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Shared small pieces
// ---------------------------------------------------------------------------

@Composable
private fun AgentAvatar(
    name: String,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initialsOf(name),
            style = if (size >= 64.dp) {
                MaterialTheme.typography.headlineSmall
            } else {
                MaterialTheme.typography.titleMedium
            },
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1
        )
    }
}

@Composable
private fun CircleIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp),
            tint = contentColor
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
            drawCircle(color = color, radius = size.minDimension * 0.24f)
        }
    }
}

@Composable
private fun SendGlyph(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(22.dp)) {
        val strokeWidth = 2.6.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, size.height * 0.5f),
            end = Offset(size.width * 0.80f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.56f, size.height * 0.26f),
            end = Offset(size.width * 0.82f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.56f, size.height * 0.74f),
            end = Offset(size.width * 0.82f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun ChevronDownGlyph(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val strokeWidth = 2.4.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.24f, size.height * 0.40f),
            end = Offset(size.width * 0.5f, size.height * 0.66f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.76f, size.height * 0.40f),
            end = Offset(size.width * 0.5f, size.height * 0.66f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun AgentProvider.label(): String = when (this) {
    AgentProvider.DeepSeek -> "DeepSeek API · быстро"
    AgentProvider.OpenRouter -> "OpenRouter API · бесплатно"
}

// ---------------------------------------------------------------------------
// Grouping / time helpers
// ---------------------------------------------------------------------------

sealed interface ChatListItem {
    data class DateHeader(val label: String) : ChatListItem
    data object WindowDivider : ChatListItem
    data class Bubble(
        val message: AgentChatMessage,
        val isFirstInGroup: Boolean,
        val isLastInGroup: Boolean,
        val outOfWindow: Boolean = false
    ) : ChatListItem
}

private fun buildChatItems(
    messages: List<AgentChatMessage>,
    windowStartIndex: Int
): List<ChatListItem> {
    val items = mutableListOf<ChatListItem>()
    var lastDayKey: Long? = null
    messages.forEachIndexed { index, message ->
        // Граница окна: всё, что выше windowStartIndex, уже не уходит в модель.
        if (windowStartIndex in 1..messages.lastIndex && index == windowStartIndex) {
            items += ChatListItem.WindowDivider
        }
        val dayKey = dayKey(message.createdAt)
        if (dayKey != null && dayKey != lastDayKey) {
            items += ChatListItem.DateHeader(dayLabel(message.createdAt))
            lastDayKey = dayKey
        }
        val prev = messages.getOrNull(index - 1)
        val next = messages.getOrNull(index + 1)
        val isFirst = prev == null ||
            prev.author != message.author ||
            (dayKey != null && dayKey(prev.createdAt) != dayKey)
        val isLast = next == null ||
            next.author != message.author ||
            (dayKey != null && dayKey(next.createdAt) != dayKey)
        items += ChatListItem.Bubble(
            message = message,
            isFirstInGroup = isFirst,
            isLastInGroup = isLast,
            outOfWindow = index < windowStartIndex
        )
    }
    return items
}

private fun dayKey(timestamp: Long): Long? {
    if (timestamp <= 0L) return null
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    return calendar.get(Calendar.YEAR) * 1000L + calendar.get(Calendar.DAY_OF_YEAR)
}

private fun dayLabel(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val today = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = timestamp }
    val sameYear = today.get(Calendar.YEAR) == target.get(Calendar.YEAR)
    val dayDiff = today.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR)
    return when {
        sameYear && dayDiff == 0 -> "Сегодня"
        sameYear && dayDiff == 1 -> "Вчера"
        sameYear -> SimpleDateFormat("d MMMM", Locale.getDefault()).format(Date(timestamp))
        else -> SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun formatTime(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private fun formatRelativeDay(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    return dayLabel(timestamp)
}

private fun initialsOf(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> "AI"
        words.size == 1 -> words[0].take(2).uppercase()
        else -> (words[0].take(1) + words[1].take(1)).uppercase()
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    AdventAITheme {
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
                        text = "Здравствуйте. Опишите страну, тип визы и вашу ситуацию.",
                        createdAt = System.currentTimeMillis()
                    ),
                    AgentChatMessage(
                        author = AgentMessageAuthor.User,
                        text = "Какие документы нужны для туристической визы?",
                        createdAt = System.currentTimeMillis()
                    )
                )
            ),
            onNavigateBack = {},
            onMessageChanged = {},
            onSendClick = {},
            onStopClick = {},
            onNewDialog = {},
            onSelectConversation = {},
            onDeleteConversation = {},
            onAgentNameChanged = {},
            onModelSelected = {},
            onSystemPromptChanged = {},
            onDialogThemeChanged = {},
            onDemoContextLimitSelected = {},
            onAutoTrimChanged = {}
        )
    }
}
