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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.designsystem.component.DocumentChecklistCard
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
import com.example.feature.chat.presentation.contextfill.ContextFillFinish
import com.example.feature.chat.presentation.contextfill.ContextFillMode
import com.example.feature.chat.presentation.contextfill.ContextFillUiState
import com.example.feature.chat.presentation.statistics.StatisticsContent
import com.example.feature.chat.presentation.statistics.StatisticsUiState
import com.example.feature.chat.presentation.statistics.StatisticsViewModel
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
    onNavigateBack: () -> Unit,
    onNavigateToCompressionAb: () -> Unit = {},
    statsViewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val statsState = statsViewModel.uiState.collectAsStateWithLifecycle()

    ChatScreen(
        state = uiState.value,
        statsState = statsState.value,
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
        onTemperatureChanged = viewModel::onTemperatureChanged,
        onMaxTokensChanged = viewModel::onMaxTokensChanged,
        onTopPChanged = viewModel::onTopPChanged,
        onCompressionToggled = viewModel::onCompressionToggled,
        onKeepRecentChanged = viewModel::onKeepRecentChanged,
        onSummarizeBatchChanged = viewModel::onSummarizeBatchChanged,
        onCompareCompression = onNavigateToCompressionAb,
        onStartContextFill = viewModel::onStartContextFill,
        onStartContextOverflow = viewModel::onStartContextOverflow,
        onStopContextFill = viewModel::onStopContextFill,
        onCloseContextFill = viewModel::onCloseContextFill
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
    statsState: StatisticsUiState,
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
    onTemperatureChanged: (Double?) -> Unit,
    onMaxTokensChanged: (Int?) -> Unit,
    onTopPChanged: (Double?) -> Unit,
    onCompressionToggled: (Boolean) -> Unit,
    onKeepRecentChanged: (Int) -> Unit,
    onSummarizeBatchChanged: (Int) -> Unit,
    onCompareCompression: () -> Unit,
    onStartContextFill: () -> Unit,
    onStartContextOverflow: () -> Unit,
    onStopContextFill: () -> Unit,
    onCloseContextFill: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettings by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val historyDrawerState = rememberDrawerState(DrawerValue.Closed)
    val statsDrawerState = rememberDrawerState(DrawerValue.Closed)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val messages = state.messages
    val isEmptyConversation = messages.none { it.author == AgentMessageAuthor.User }
    val chatItems = remember(messages) { buildChatItems(messages) }
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

    Box(modifier = modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = historyDrawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.fillMaxWidth(0.86f),
                    drawerShape = RoundedCornerShape(topEnd = AppRadii.sheet, bottomEnd = AppRadii.sheet)
                ) {
                    ConversationsSheet(
                        conversations = state.conversations,
                        activeConversationId = state.activeConversationId,
                        onNewDialog = {
                            scope.launch { historyDrawerState.close() }
                            onNewDialog()
                        },
                        onSelectConversation = { id ->
                            scope.launch { historyDrawerState.close() }
                            onSelectConversation(id)
                        },
                        onDeleteConversation = onDeleteConversation,
                        onClose = { scope.launch { historyDrawerState.close() } }
                    )
                }
            }
        ) {
            // Material3 даёт только левый drawer; правую шторку (статистика) делаем через RTL.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                ModalNavigationDrawer(
                    drawerState = statsDrawerState,
                    drawerContent = {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            ModalDrawerSheet(
                                modifier = Modifier.fillMaxWidth(0.92f),
                                drawerShape = RoundedCornerShape(topStart = AppRadii.sheet, bottomStart = AppRadii.sheet)
                            ) {
                                StatisticsContent(
                                    state = statsState,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            AgentHeader(
                                config = state.config,
                                isTyping = state.isLoading,
                                onNavigateBack = onNavigateBack,
                                onHistoryClick = { scope.launch { historyDrawerState.open() } },
                                onStatsClick = { scope.launch { statsDrawerState.open() } },
                                onSettingsClick = { showSettings = true }
                            )

                            if (state.contextFill.active) {
                                ContextFillBanner(
                                    fill = state.contextFill,
                                    onStop = onStopContextFill,
                                    onClose = onCloseContextFill
                                )
                            }

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
                                        errorMessage = state.errorMessage,
                                        outOfWindowCount = if (state.contextFill.active) {
                                            state.contextFill.droppedCount
                                        } else {
                                            0
                                        },
                                        summarizedCount = state.summarizedCount,
                                        summaryText = state.summaryText,
                                        summaryTokens = state.summaryTokens
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
                                savingsPercent = state.compressionSavingsPercent,
                                fullTokens = state.fullContextTokens
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
                    }
                }
            }
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
                    onTemperatureChanged = onTemperatureChanged,
                    onMaxTokensChanged = onMaxTokensChanged,
                    onTopPChanged = onTopPChanged,
                    onCompressionToggled = onCompressionToggled,
                    onKeepRecentChanged = onKeepRecentChanged,
                    onSummarizeBatchChanged = onSummarizeBatchChanged,
                    onCompareCompression = {
                        showSettings = false
                        onCompareCompression()
                    },
                    onStartContextFill = {
                        showSettings = false
                        onStartContextFill()
                    },
                    onStartContextOverflow = {
                        showSettings = false
                        onStartContextOverflow()
                    },
                    onClose = { showSettings = false }
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
    onHistoryClick: () -> Unit,
    onStatsClick: () -> Unit,
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
                contentDescription = "История диалогов",
                onClick = onHistoryClick
            )
            CircleIconButton(
                iconRes = R.drawable.ic_stats_24,
                contentDescription = "Статистика",
                onClick = onStatsClick
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
    modifier: Modifier = Modifier,
    outOfWindowCount: Int = 0,
    summarizedCount: Int = 0,
    summaryText: String? = null,
    summaryTokens: Int = 0
) {
    // Индекс первого сообщения, которое ещё в окне: перед ним рисуем границу «модель это не видит».
    val dividerIndex = remember(chatItems, outOfWindowCount) {
        windowDividerIndex(chatItems, outOfWindowCount)
    }
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
        if (summarizedCount > 0) {
            item(key = "compressed-memory") {
                CompressedMemoryCard(
                    count = summarizedCount,
                    summary = summaryText,
                    summaryTokens = summaryTokens
                )
            }
        }
        itemsIndexed(chatItems) { index, item ->
            if (index == dividerIndex) {
                WindowDivider()
            }
            val outOfWindow = item is ChatListItem.Bubble && dividerIndex >= 0 && index < dividerIndex
            when (item) {
                is ChatListItem.DateHeader -> DateSeparator(label = item.label)
                is ChatListItem.Bubble -> MessageBubble(
                    message = item.message,
                    isLastInGroup = item.isLastInGroup,
                    topSpacing = if (item.isFirstInGroup) AppSpacing.sm else 0.dp,
                    outOfWindow = outOfWindow
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

/** Индекс в [chatItems] первого «пузыря», который ещё попадает в окно (перед ним — граница). */
private fun windowDividerIndex(chatItems: List<ChatListItem>, outOfWindowCount: Int): Int {
    if (outOfWindowCount <= 0) return -1
    var bubbleSeen = 0
    chatItems.forEachIndexed { index, item ->
        if (item is ChatListItem.Bubble) {
            if (bubbleSeen == outOfWindowCount) return index
            bubbleSeen++
        }
    }
    return -1
}

@Composable
private fun WindowDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Text(
                text = stringResource(R.string.window_divider_label),
                modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = 5.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CompressedMemoryCard(
    count: Int,
    summary: String?,
    summaryTokens: Int,
    modifier: Modifier = Modifier
) {
    var expanded by remember(summary) { mutableStateOf(false) }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = AppSpacing.sm),
        shape = RoundedCornerShape(AppRadii.cardLarge),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = stringResource(R.string.compression_memory_card_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1
            )
            Text(
                text = stringResource(
                    R.string.compression_memory_card_subtitle,
                    count,
                    formatTokens(summaryTokens)
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            if (!summary.isNullOrBlank()) {
                if (expanded) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Text(
                    text = stringResource(
                        if (expanded) {
                            R.string.compression_memory_collapse
                        } else {
                            R.string.compression_memory_expand
                        }
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppRadii.field))
                        .clickable { expanded = !expanded }
                        .padding(vertical = 2.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
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
            .padding(top = topSpacing)
            .then(if (outOfWindow) Modifier.alpha(0.45f) else Modifier),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 340.dp),
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
        if (outOfWindow) {
            Text(
                text = stringResource(R.string.message_out_of_window),
                modifier = Modifier.padding(top = 3.dp, start = 6.dp, end = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                maxLines = 1
            )
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
    modifier: Modifier = Modifier,
    savingsPercent: Int = 0,
    fullTokens: Int = 0
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
        savingsPercent > 0 -> stringResource(
            R.string.context_meter_savings,
            savingsPercent,
            formatTokens(fullTokens)
        )
        overflow -> stringResource(R.string.context_meter_overflow_block)
        warning -> stringResource(R.string.context_meter_warning)
        else -> null
    }
    val statusColor = if (savingsPercent > 0) AdventTheme.status.success else barColor
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
                    color = statusColor,
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
            .fillMaxSize()
            .statusBarsPadding()
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
    onTemperatureChanged: (Double?) -> Unit,
    onMaxTokensChanged: (Int?) -> Unit,
    onTopPChanged: (Double?) -> Unit,
    onCompressionToggled: (Boolean) -> Unit,
    onKeepRecentChanged: (Int) -> Unit,
    onSummarizeBatchChanged: (Int) -> Unit,
    onCompareCompression: () -> Unit,
    onStartContextFill: () -> Unit,
    onStartContextOverflow: () -> Unit,
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

        SettingsSection(title = stringResource(R.string.settings_section_compression)) {
            CompressionSettings(
                enabled = state.config.compressionEnabled,
                keepRecent = state.config.keepRecentMessages,
                summarizeBatch = state.config.summarizeBatch,
                onToggled = onCompressionToggled,
                onKeepRecentChanged = onKeepRecentChanged,
                onSummarizeBatchChanged = onSummarizeBatchChanged,
                onCompare = onCompareCompression
            )
        }

        SettingsSection(title = stringResource(R.string.settings_section_context)) {
            StartContextFillButton(onClick = onStartContextFill)
            OverflowDemoButton(onClick = onStartContextOverflow)
        }

        SettingsSection(title = stringResource(R.string.settings_section_api)) {
            ParamChips(
                title = stringResource(R.string.settings_temperature_label),
                options = TemperaturePresets,
                selected = state.config.temperature,
                onSelect = onTemperatureChanged
            )
            ParamChips(
                title = stringResource(R.string.settings_top_p_label),
                options = TopPPresets,
                selected = state.config.topP,
                onSelect = onTopPChanged
            )
            ParamChips(
                title = stringResource(R.string.settings_max_tokens_label),
                options = MaxTokensPresets,
                selected = state.config.maxTokens,
                onSelect = onMaxTokensChanged
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

private val TemperaturePresets: List<Double?> = listOf(null, 0.0, 0.3, 0.7, 1.0, 1.5)
private val TopPPresets: List<Double?> = listOf(null, 0.5, 0.8, 0.9, 1.0)
private val MaxTokensPresets: List<Int?> = listOf(null, 256, 512, 1024, 2048)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ParamChips(
    title: String,
    options: List<T?>,
    selected: T?,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            options.forEach { value ->
                SelectableChip(
                    text = value?.toString() ?: stringResource(R.string.settings_param_default),
                    selected = value == selected,
                    onClick = { onSelect(value) }
                )
            }
        }
    }
}

@Composable
private fun StartContextFillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppRadii.bubble))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(AppRadii.bubble),
            color = MaterialTheme.colorScheme.primary
        ) {
            Row(
                modifier = Modifier.padding(AppSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_stats_24),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = stringResource(R.string.context_fill_start_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Text(
            text = stringResource(R.string.context_fill_start_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OverflowDemoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppRadii.bubble))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(AppRadii.bubble),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.error
            )
        ) {
            Row(
                modifier = Modifier.padding(AppSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_24),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(R.string.context_overflow_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        Text(
            text = stringResource(R.string.context_overflow_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val KeepRecentPresets = listOf(2, 4, 6, 8, 10)
private val SummarizeBatchPresets = listOf(4, 6, 8, 10)

@Composable
private fun CompressionSettings(
    enabled: Boolean,
    keepRecent: Int,
    summarizeBatch: Int,
    onToggled: (Boolean) -> Unit,
    onKeepRecentChanged: (Int) -> Unit,
    onSummarizeBatchChanged: (Int) -> Unit,
    onCompare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.compression_toggle_label),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(checked = enabled, onCheckedChange = onToggled)
        }
        Text(
            text = stringResource(R.string.compression_toggle_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (enabled) {
            IntChips(
                title = stringResource(R.string.compression_keep_recent_label),
                options = KeepRecentPresets,
                selected = keepRecent,
                onSelect = onKeepRecentChanged
            )
            IntChips(
                title = stringResource(R.string.compression_batch_label),
                options = SummarizeBatchPresets,
                selected = summarizeBatch,
                onSelect = onSummarizeBatchChanged
            )
        }
        CompareCompressionButton(onClick = onCompare)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntChips(
    title: String,
    options: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            options.forEach { value ->
                SelectableChip(
                    text = value.toString(),
                    selected = value == selected,
                    onClick = { onSelect(value) }
                )
            }
        }
    }
}

@Composable
private fun CompareCompressionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppRadii.bubble))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(AppRadii.bubble),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Row(
                modifier = Modifier.padding(AppSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_stats_24),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = stringResource(R.string.compression_compare_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Text(
            text = stringResource(R.string.compression_compare_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContextFillBanner(
    fill: ContextFillUiState,
    onStop: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isError = fill.finish == ContextFillFinish.Error
    val isOverflow = fill.mode == ContextFillMode.Overflow
    // Тонированная карточка (а не «белый прямоугольник»): на ошибке — error-контейнер.
    val containerColor = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val onContainer = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val subColor = onContainer.copy(alpha = 0.72f)
    val accent = when {
        isError -> MaterialTheme.colorScheme.error
        !isOverflow && !fill.nameInWindow -> AdventTheme.status.warning
        else -> MaterialTheme.colorScheme.primary
    }
    val finishText: String? = when (fill.finish) {
        ContextFillFinish.Error ->
            fill.errorText?.let { stringResource(R.string.context_fill_finish_error, it) }
        ContextFillFinish.MaxTurns ->
            if (isOverflow) "Промпт уместился — переполнения не случилось."
            else stringResource(R.string.context_fill_finish_maxturns, fill.turn)
        ContextFillFinish.Cancelled ->
            stringResource(R.string.context_fill_finish_cancelled)
        null -> null
    }
    val title = stringResource(
        when {
            isOverflow && fill.running -> R.string.context_overflow_banner_running
            isOverflow -> R.string.context_overflow_banner_done
            fill.running -> R.string.context_fill_banner_title_running
            else -> R.string.context_fill_banner_title_done
        }
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        shape = RoundedCornerShape(AppRadii.cardLarge),
        color = containerColor,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (fill.running) {
                        TypingDots(color = accent, dotSize = 5.dp)
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = onContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                fill.modelTitle?.let {
                    Text(
                        text = stringResource(R.string.context_fill_model, it),
                        style = MaterialTheme.typography.labelSmall,
                        color = subColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isOverflow) {
                    Text(
                        text = stringResource(
                            R.string.context_overflow_prompt,
                            formatTokens(fill.tokensSent),
                            formatTokens(fill.modelWindow)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = subColor,
                        maxLines = 2
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.context_fill_turn,
                            fill.turn,
                            formatTokens(fill.tokensSent)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = subColor,
                        maxLines = 1
                    )
                    Text(
                        text = if (fill.nameInWindow) {
                            stringResource(R.string.context_fill_name_in_window, fill.name)
                        } else {
                            stringResource(R.string.context_fill_name_out_window, fill.name)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (fill.nameInWindow) subColor else AdventTheme.status.warning,
                        maxLines = 1
                    )
                    Text(
                        text = when (fill.nameRecalled) {
                            true -> stringResource(R.string.context_fill_name_recalled)
                            false -> stringResource(R.string.context_fill_name_forgotten)
                            null -> stringResource(R.string.context_fill_name_unknown)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = when (fill.nameRecalled) {
                            true -> AdventTheme.status.success
                            false -> MaterialTheme.colorScheme.error
                            null -> subColor
                        },
                        maxLines = 1
                    )
                }

                if (fill.running) {
                    fill.note?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.labelSmall,
                            color = AdventTheme.status.warning,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                finishText?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isError) onContainer else accent,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (fill.running) {
                BannerActionButton(text = stringResource(R.string.context_fill_stop), onClick = onStop)
            } else {
                BannerActionButton(text = stringResource(R.string.context_fill_close), onClick = onClose)
            }
        }
    }
}

@Composable
private fun BannerActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1
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
    data class Bubble(
        val message: AgentChatMessage,
        val isFirstInGroup: Boolean,
        val isLastInGroup: Boolean
    ) : ChatListItem
}

private fun buildChatItems(messages: List<AgentChatMessage>): List<ChatListItem> {
    val items = mutableListOf<ChatListItem>()
    var lastDayKey: Long? = null
    messages.forEachIndexed { index, message ->
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
            isLastInGroup = isLast
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
            statsState = StatisticsUiState(),
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
            onTemperatureChanged = {},
            onMaxTokensChanged = {},
            onTopPChanged = {},
            onCompressionToggled = {},
            onKeepRecentChanged = {},
            onSummarizeBatchChanged = {},
            onCompareCompression = {},
            onStartContextFill = {},
            onStartContextOverflow = {},
            onStopContextFill = {},
            onCloseContextFill = {}
        )
    }
}
