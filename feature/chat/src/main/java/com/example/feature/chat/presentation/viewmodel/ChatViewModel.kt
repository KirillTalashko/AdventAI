package com.example.feature.chat.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.AppError
import com.example.core.common.AppResult
import com.example.core.common.fold
import com.example.core.domain.agent.AiAgent
import com.example.core.domain.agent.ContextWindowTrimmer
import com.example.core.domain.agent.HistoryCompressor
import com.example.core.domain.agent.TokenEstimator
import com.example.core.domain.di.ApplicationScope
import com.example.core.domain.repository.ChatHistoryRepository
import com.example.core.domain.repository.ConversationSummaryState
import com.example.core.domain.usecase.PrepareCompressedContextUseCase
import com.example.core.model.ai.AgentAnswer
import com.example.core.model.ai.AgentChatMessage
import com.example.core.model.ai.AgentConfig
import com.example.core.model.ai.AgentLlmModel
import com.example.core.model.ai.AgentMessageAuthor
import com.example.core.model.ai.AgentProvider
import com.example.core.model.ai.Conversation
import com.example.core.model.ai.effectiveContextLimit
import com.example.feature.chat.presentation.contextfill.ContextFillFinish
import com.example.feature.chat.presentation.contextfill.ContextFillMode
import com.example.feature.chat.presentation.contextfill.ContextFillQuestions
import com.example.feature.chat.presentation.contextfill.ContextFillUiState
import com.example.feature.chat.presentation.mapper.ChatErrorMessageMapper
import com.example.feature.chat.presentation.navigation.ChatDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

private const val DEFAULT_CONVERSATION_TITLE = "Новый диалог"
private const val TITLE_MAX_LENGTH = 40

// --- Демо-заливка контекста (Day 9) ---
private const val FILL_SANDBOX_TITLE = "Стресс-тест контекста"
/** Имя, которое называем агенту в начале — потом проверяем, помнит ли он его. */
private const val FILL_TEST_NAME = "Кирилл"
/**
 * Демо-окно: контекст, реально уходящий в модель, держим около этого бюджета. Меньше реального
 * окна free-модели (131k), чтобы «забывание» имени наступало за разумное число ходов.
 */
private const val FILL_WINDOW_TOKENS = 6_000
/** Защитный лимит ходов (он же штатное завершение демо на DeepSeek — провайдер не падает). */
private const val MAX_FILL_TURNS = 40
/**
 * Небольшая пауза между запросами — чтобы стрим был наблюдаемым и не долбить API.
 * DeepSeek не лимитирован так жёстко, как free-OpenRouter, так что хватает долей секунды.
 */
private const val FILL_REQUEST_INTERVAL_MS = 600L
/** Сколько раз повторить запрос при временной ошибке (перегрузка/таймаут), прежде чем сдаться. */
private const val FILL_MAX_RETRIES = 3
/** База backoff между повторами (умножается на номер попытки, с потолком). */
private const val FILL_RETRY_BACKOFF_MS = 3_000L
private const val FILL_RETRY_BACKOFF_CAP_MS = 10_000L
/** Во сколько раз промпт-переполнение превышает окно модели (для наглядности). */
private const val OVERFLOW_WINDOW_MULTIPLIER = 2

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val agent: AiAgent,
    private val chatHistoryRepository: ChatHistoryRepository,
    private val prepareCompressedContext: PrepareCompressedContextUseCase,
    private val errorMessageMapper: ChatErrorMessageMapper,
    @ApplicationScope private val appScope: CoroutineScope
) : ViewModel() {
    private val agentId: String =
        savedStateHandle[ChatDestination.AGENT_ID_ARGUMENT] ?: ChatDestination.VISA_AGENT_ID

    private val activeConversationId = MutableStateFlow<Long?>(null)

    /** Текущая задача генерации ответа — храним, чтобы её можно было отменить кнопкой «стоп». */
    private var sendJob: Job? = null

    /** Фоновая задача демо-заливки контекста. */
    private var fillJob: Job? = null

    /** id эфемерного диалога стресс-теста (для очистки). */
    private var fillSandboxId: Long? = null

    /** Первый запрос заливки идёт без паузы, остальные — с интервалом. */
    private var fillFirstRequest: Boolean = true

    /** Состояние сжатия активного диалога (summary + сколько свёрнуто) — кэш для индикатора окна. */
    private var summaryState: ConversationSummaryState = ConversationSummaryState.Empty

    private val _uiState = MutableStateFlow(
        AgentChatUiState(config = defaultConfig(agentId)).withContextEstimate()
    )
    val uiState: StateFlow<AgentChatUiState> = _uiState.asStateFlow()

    init {
        // Подчищаем эфемерные данные прошлой сессии (стресс-тест мог пережить смерть процесса).
        viewModelScope.launch {
            chatHistoryRepository.deleteEphemeralConversations(agentId)
            chatHistoryRepository.clearFillerQuestions()
        }
        // Открываем последний диалог агента; если их ещё нет — создаём первый.
        viewModelScope.launch {
            val latest = chatHistoryRepository.latestConversationId(agentId)
            activeConversationId.value = latest ?: createConversationWithGreeting()
        }
        // Список диалогов агента для переключения.
        viewModelScope.launch {
            chatHistoryRepository.observeConversations(agentId).collect { conversations ->
                _uiState.update { state -> state.copy(conversations = conversations) }
            }
        }
        // Сообщения активного диалога; при переключении поток пересоздаётся.
        viewModelScope.launch {
            activeConversationId.filterNotNull().collectLatest { conversationId ->
                summaryState = chatHistoryRepository.getSummaryState(conversationId)
                _uiState.update { state -> state.copy(activeConversationId = conversationId) }
                chatHistoryRepository.observeMessages(conversationId).collect { messages ->
                    _uiState.update { state -> state.copy(messages = messages).withContextEstimate() }
                }
            }
        }
    }

    fun onMessageChanged(message: String) {
        _uiState.update { state ->
            state.copy(message = message).withContextEstimate()
        }
    }

    fun sendMessage() {
        val prompt = uiState.value.message.trim()
        if (prompt.isBlank()) {
            _uiState.update { state ->
                state.copy(errorMessage = errorMessageMapper.emptyMessage())
            }
            return
        }

        val conversationId = activeConversationId.value ?: return
        val config = uiState.value.config
        _uiState.update { state ->
            state.copy(message = "", isLoading = true, errorMessage = null)
        }

        sendJob = viewModelScope.launch {
            val isFirstUserMessage = chatHistoryRepository.getMessages(conversationId)
                .none { it.author == AgentMessageAuthor.User }

            chatHistoryRepository.appendMessage(
                conversationId = conversationId,
                message = AgentChatMessage(
                    author = AgentMessageAuthor.User,
                    text = prompt,
                    createdAt = System.currentTimeMillis()
                )
            )
            if (isFirstUserMessage) {
                // Авто-заголовок диалога по первому сообщению пользователя (как в ChatGPT).
                chatHistoryRepository.updateConversationTitle(conversationId, prompt.toConversationTitle())
            }

            val conversation = chatHistoryRepository.getMessages(conversationId)
            // Day 9: при включённом сжатии старая часть истории сворачивается в summary,
            // и в модель уходит `summary + последние N сообщений` вместо полной истории.
            val prepared = prepareCompressedContext(
                conversationId = conversationId,
                conversation = conversation,
                config = config
            )
            summaryState = ConversationSummaryState(
                summary = prepared.memory,
                summarizedCount = prepared.summarizedCount
            )
            agent.ask(
                config = config,
                conversation = prepared.recent,
                memory = prepared.memory
            ).fold(
                onSuccess = { answer ->
                    chatHistoryRepository.appendMessage(
                        conversationId = conversationId,
                        message = AgentChatMessage(
                            author = AgentMessageAuthor.Agent,
                            text = answer.content,
                            createdAt = System.currentTimeMillis(),
                            usage = answer.usage,
                            modelApiId = config.model.apiId
                        )
                    )
                    chatHistoryRepository.touchConversation(conversationId)
                    _uiState.update { state -> state.copy(isLoading = false) }
                },
                onError = { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = errorMessageMapper.map(error)
                        )
                    }
                }
            )
        }
    }

    /** Прерывает текущую генерацию (кнопка «стоп» в композере). Запрос отменяется. */
    fun onStopGeneration() {
        sendJob?.cancel()
        sendJob = null
        _uiState.update { state -> state.copy(isLoading = false) }
    }

    fun onNewDialog() {
        viewModelScope.launch {
            activeConversationId.value = createConversationWithGreeting()
        }
    }

    fun onSelectConversation(conversationId: Long) {
        activeConversationId.value = conversationId
    }

    fun onDeleteConversation(conversationId: Long) {
        viewModelScope.launch {
            chatHistoryRepository.deleteConversation(conversationId)
            if (activeConversationId.value == conversationId) {
                activeConversationId.value = chatHistoryRepository.latestConversationId(agentId)
                    ?: createConversationWithGreeting()
            }
        }
    }

    fun onAgentNameChanged(value: String) {
        updateConfig { copy(name = value) }
    }

    fun onModelSelected(model: AgentLlmModel) {
        updateConfig { copy(model = model) }
    }

    /** Параметры API (null — значение по умолчанию у провайдера). */
    fun onTemperatureChanged(value: Double?) {
        updateConfig { copy(temperature = value) }
    }

    fun onMaxTokensChanged(value: Int?) {
        updateConfig { copy(maxTokens = value) }
    }

    fun onTopPChanged(value: Double?) {
        updateConfig { copy(topP = value) }
    }

    // --- Сжатие истории (Day 9) ---

    fun onCompressionToggled(enabled: Boolean) {
        updateConfig { copy(compressionEnabled = enabled) }
    }

    fun onKeepRecentChanged(value: Int) {
        updateConfig { copy(keepRecentMessages = value) }
    }

    fun onSummarizeBatchChanged(value: Int) {
        updateConfig { copy(summarizeBatch = value) }
    }

    // -----------------------------------------------------------------------
    // Демо-заливка контекста: фоновый авто-диалог на DeepSeek (дёшево и быстро,
    // без жёстких rate-limit'ов free-тарифа). Забивает окно визовыми вопросами;
    // по ходу видно, как старые сообщения (включая имя) выпадают из окна и модель
    // перестаёт помнить имя. Завершается на защитном лимите ходов.
    // -----------------------------------------------------------------------

    fun onStartContextFill() {
        if (uiState.value.contextFill.running) return
        fillJob = viewModelScope.launch { runContextFill() }
    }

    /** Демо переполнения: один огромный промпт в DeepSeek → реальная ошибка переполнения окна. */
    fun onStartContextOverflow() {
        if (uiState.value.contextFill.running) return
        fillJob = viewModelScope.launch { runOverflowDemo() }
    }

    /** Остановить заливку по кнопке. */
    fun onStopContextFill() {
        fillJob?.cancel()
        fillJob = null
        updateFill { copy(running = false, finish = ContextFillFinish.Cancelled) }
    }

    /** Закрыть баннер и вернуться к обычному диалогу; эфемерный стресс-тест удаляем. */
    fun onCloseContextFill() {
        fillJob?.cancel()
        fillJob = null
        val sandboxId = fillSandboxId
        fillSandboxId = null
        updateFill { ContextFillUiState.Idle }
        // Переключать активный диалог нужно только если был создан эфемерный sandbox (авто-диалог).
        if (sandboxId != null) {
            viewModelScope.launch {
                chatHistoryRepository.deleteConversation(sandboxId)
                chatHistoryRepository.clearFillerQuestions()
                activeConversationId.value = chatHistoryRepository.latestConversationId(agentId)
                    ?: createConversationWithGreeting()
            }
        }
    }

    private suspend fun runOverflowDemo() {
        val testModel = uiState.value.config.model
            .takeIf { it.provider == AgentProvider.DeepSeek }
            ?: AgentLlmModel.DeepSeekFlash
        // Проверку окна делает сам агент (demoContextLimitTokens → ContextOverflow): надёжно,
        // т.к. deepseek-v4 на слишком длинный промпт ошибку не отдаёт, а просто отвечает.
        val testConfig = uiState.value.config.copy(
            model = testModel,
            demoContextLimitTokens = testModel.maxContextTokens
        )

        val hugePrompt = buildOverflowPrompt(testModel.maxContextTokens)
        val promptTokens = TokenEstimator.estimateText(hugePrompt)

        updateFill {
            ContextFillUiState(
                active = true,
                running = true,
                mode = ContextFillMode.Overflow,
                modelTitle = testModel.title,
                modelWindow = testModel.maxContextTokens,
                tokensSent = promptTokens
            )
        }

        // Огромный промпт не помещается в окно модели → агент возвращает ContextOverflow.
        val request = listOf(
            AgentChatMessage(AgentMessageAuthor.User, hugePrompt, System.currentTimeMillis())
        )
        when (val result = agent.ask(testConfig, request)) {
            is AppResult.Success -> {
                // Неожиданно уместилось — показываем как завершение без ошибки.
                updateFill { copy(running = false, finish = ContextFillFinish.MaxTurns) }
            }

            is AppResult.Error -> finishFillWithError(result.error)
        }
    }

    /** Собирает промпт, заведомо превышающий контекстное окно модели (с запасом). */
    private fun buildOverflowPrompt(windowTokens: Int): String {
        val unit = "Это длинный демонстрационный фрагмент текста для переполнения контекстного окна модели. "
        val unitTokens = TokenEstimator.estimateText(unit).coerceAtLeast(1)
        val targetTokens = windowTokens * OVERFLOW_WINDOW_MULTIPLIER + 2_000
        val repeats = targetTokens / unitTokens + 1
        return buildString {
            append("Проанализируй и подробно перескажи следующий текст целиком:\n")
            repeat(repeats) { append(unit) }
        }
    }

    private suspend fun runContextFill() {
        // Заливаем на DeepSeek (быстро/дёшево), даже если в чате выбрана free-модель.
        val testModel = uiState.value.config.model
            .takeIf { it.provider == AgentProvider.DeepSeek }
            ?: AgentLlmModel.DeepSeekFlash
        val testConfig = uiState.value.config.copy(
            model = testModel,
            demoContextLimitTokens = null
        )
        fillFirstRequest = true
        val systemPrompt = agent.systemPromptOf(testConfig)

        // Банк вопросов в Room (session-scoped), расходуем по кругу.
        chatHistoryRepository.seedFillerQuestions(ContextFillQuestions.bank)
        val questions = chatHistoryRepository.getUnusedFillerQuestions()
        if (questions.isEmpty()) {
            updateFill { copy(active = true, running = false, finish = ContextFillFinish.MaxTurns) }
            return
        }

        // Отдельный эфемерный диалог — чтобы не засорять реальную историю и почистить в конце.
        val sandboxId = chatHistoryRepository.createEphemeralConversation(agentId, FILL_SANDBOX_TITLE)
        fillSandboxId = sandboxId
        activeConversationId.value = sandboxId

        updateFill {
            ContextFillUiState(
                active = true,
                running = true,
                modelTitle = testModel.title,
                name = FILL_TEST_NAME
            )
        }

        // Шаг 1: называем имя.
        appendUser(
            sandboxId,
            "Меня зовут $FILL_TEST_NAME. Запомни моё имя — в конце диалога я спрошу, как меня зовут."
        )
        if (askSandbox(sandboxId, testConfig, systemPrompt) == null) return

        var nameProbed = false
        var turn = 0
        var index = 0
        while (turn < MAX_FILL_TURNS && coroutineContext.isActive) {
            val question = questions[index % questions.size]
            val variant = index
            index++
            chatHistoryRepository.markFillerUsed(question.id)

            // «Тяжёлый» вопрос под чередующийся сценарий: большой разнообразный контекст быстро
            // забивает окно, и короткое сообщение с именем выпадает из контекста за пару ходов.
            appendUser(sandboxId, ContextFillQuestions.weighty(question.text, variant))
            val nameInWindow = currentNameInWindow(sandboxId, systemPrompt)
            updateFill { copy(turn = turn + 1, nameInWindow = nameInWindow) }

            if (askSandbox(sandboxId, testConfig, systemPrompt) == null) return
            turn++

            // Как только имя выпало из окна — проверяем, помнит ли его модель.
            if (!nameInWindow && !nameProbed) {
                nameProbed = true
                if (!probeName(sandboxId, testConfig, systemPrompt)) return
            }
        }

        // Дошли до защитного лимита — финальная проверка имени и штатное завершение.
        if (coroutineContext.isActive) {
            probeName(sandboxId, testConfig, systemPrompt)
            updateFill { copy(running = false, note = null, finish = ContextFillFinish.MaxTurns) }
        }
    }

    /**
     * Отправляет текущий контекст (со сдвигающимся окном) в модель теста и сохраняет ответ.
     * Лёгкая пауза между запросами + ретраи с backoff на временных сбоях (перегрузка/таймаут).
     * Возвращает ответ или null (заливка завершена — ошибкой или отменой).
     */
    private suspend fun askSandbox(
        conversationId: Long,
        testConfig: AgentConfig,
        systemPrompt: String
    ): AgentAnswer? {
        val full = chatHistoryRepository.getMessages(conversationId)
        val trim = ContextWindowTrimmer.trim(systemPrompt, full, FILL_WINDOW_TOKENS)
        updateFill { copy(tokensSent = trim.estimatedTokens, droppedCount = trim.droppedCount) }

        var attempt = 0
        while (coroutineContext.isActive) {
            paceBeforeRequest()
            when (val result = agent.ask(testConfig, trim.kept)) {
                is AppResult.Success -> {
                    updateFill { copy(note = null) }
                    appendAgentAnswer(conversationId, result.data, testConfig.model.apiId)
                    return result.data
                }

                is AppResult.Error -> {
                    if (isRetriable(result.error) && attempt < FILL_MAX_RETRIES) {
                        attempt++
                        val waitMs = (FILL_RETRY_BACKOFF_MS * attempt)
                            .coerceAtMost(FILL_RETRY_BACKOFF_CAP_MS)
                        updateFill { copy(note = "Провайдер занят: пауза ${waitMs / 1000} с, повтор…") }
                        delay(waitMs)
                        continue
                    }
                    finishFillWithError(result.error)
                    return null
                }
            }
        }
        return null
    }

    /** Спрашивает имя и фиксирует, помнит ли его модель. Возвращает false при ошибке провайдера. */
    private suspend fun probeName(
        conversationId: Long,
        testConfig: AgentConfig,
        systemPrompt: String
    ): Boolean {
        appendUser(conversationId, "Кстати, как меня зовут? Ответь только именем.")
        val answer = askSandbox(conversationId, testConfig, systemPrompt) ?: return false
        val recalled = answer.content.contains(FILL_TEST_NAME, ignoreCase = true)
        updateFill { copy(nameRecalled = recalled) }
        return true
    }

    private suspend fun currentNameInWindow(conversationId: Long, systemPrompt: String): Boolean {
        val full = chatHistoryRepository.getMessages(conversationId)
        return ContextWindowTrimmer.trim(systemPrompt, full, FILL_WINDOW_TOKENS).droppedCount == 0
    }

    /** Лёгкая пауза между запросами для наблюдаемого стрима (первый запрос — без паузы). */
    private suspend fun paceBeforeRequest() {
        if (fillFirstRequest) {
            fillFirstRequest = false
        } else {
            delay(FILL_REQUEST_INTERVAL_MS)
        }
    }

    private fun isRetriable(error: AppError): Boolean = when (error) {
        AppError.RateLimit,
        AppError.ServerOverloaded,
        AppError.Server,
        AppError.Timeout -> true

        else -> false
    }

    private fun finishFillWithError(error: AppError) {
        updateFill {
            copy(
                running = false,
                note = null,
                finish = ContextFillFinish.Error,
                errorText = errorMessageMapper.map(error)
            )
        }
    }

    private suspend fun appendUser(conversationId: Long, text: String) {
        chatHistoryRepository.appendMessage(
            conversationId,
            AgentChatMessage(AgentMessageAuthor.User, text, System.currentTimeMillis())
        )
    }

    private suspend fun appendAgentAnswer(
        conversationId: Long,
        answer: AgentAnswer,
        modelApiId: String
    ) {
        chatHistoryRepository.appendMessage(
            conversationId,
            AgentChatMessage(
                author = AgentMessageAuthor.Agent,
                text = answer.content,
                createdAt = System.currentTimeMillis(),
                usage = answer.usage,
                modelApiId = modelApiId
            )
        )
        chatHistoryRepository.touchConversation(conversationId)
    }

    private fun updateFill(update: ContextFillUiState.() -> ContextFillUiState) {
        _uiState.update { state -> state.copy(contextFill = state.contextFill.update()) }
    }

    override fun onCleared() {
        super.onCleared()
        fillJob?.cancel()
        // viewModelScope уже отменён — чистим session-данные на app-scope.
        val id = agentId
        appScope.launch {
            withContext(NonCancellable) {
                chatHistoryRepository.deleteEphemeralConversations(id)
                chatHistoryRepository.clearFillerQuestions()
            }
        }
    }

    fun onSystemPromptChanged(value: String) {
        updateConfig { copy(systemPrompt = value) }
    }

    fun onDialogThemeChanged(value: String) {
        updateConfig { copy(dialogTheme = value) }
    }

    private fun updateConfig(update: AgentConfig.() -> AgentConfig) {
        _uiState.update { state ->
            state.copy(config = state.config.update()).withContextEstimate()
        }
    }

    /** Пересчитывает оценку токенов контекста и лимит окна по текущему состоянию. */
    private fun AgentChatUiState.withContextEstimate(): AgentChatUiState {
        val fullTokens = agent.estimateContextTokens(config, messages, message)
        val limit = config.effectiveContextLimit()
        if (!config.compressionEnabled) {
            return copy(
                contextTokens = fullTokens,
                fullContextTokens = fullTokens,
                contextLimit = limit,
                summarizedCount = 0,
                summaryText = null,
                summaryTokens = 0
            )
        }
        // Сжатие включено: считаем, сколько реально уйдёт в модель (summary + сырой хвост).
        val plan = HistoryCompressor.plan(
            conversation = messages,
            alreadySummarized = summaryState.summarizedCount,
            keepRecent = config.keepRecentMessages,
            batch = config.summarizeBatch
        )
        val memory = summaryState.summary?.takeIf { it.isNotBlank() }
        val compressedTokens = agent.estimateCompressedContextTokens(config, plan.recent, memory, message)
        return copy(
            contextTokens = compressedTokens,
            fullContextTokens = fullTokens,
            contextLimit = limit,
            summarizedCount = summaryState.summarizedCount,
            summaryText = memory,
            summaryTokens = memory?.let { TokenEstimator.estimateText(it) } ?: 0
        )
    }

    private suspend fun createConversationWithGreeting(): Long {
        val id = chatHistoryRepository.createConversation(agentId, DEFAULT_CONVERSATION_TITLE)
        chatHistoryRepository.appendMessage(id, greetingMessage())
        return id
    }

    private fun String.toConversationTitle(): String {
        val singleLine = trim().replace("\n", " ")
        return if (singleLine.length <= TITLE_MAX_LENGTH) {
            singleLine
        } else {
            singleLine.take(TITLE_MAX_LENGTH).trimEnd() + "…"
        }
    }

    private fun greetingMessage(): AgentChatMessage =
        AgentChatMessage(
            author = AgentMessageAuthor.Agent,
            text = if (agentId == ChatDestination.NEW_AGENT_ID) {
                "Привет! Я ваш новый AI-агент. Опишите задачу — я помогу."
            } else {
                "Здравствуйте. Я помогу подготовиться к визовому процессу: разберу ситуацию, документы, риски и следующие шаги."
            },
            createdAt = System.currentTimeMillis()
        )

    private fun defaultConfig(agentId: String): AgentConfig =
        if (agentId == ChatDestination.NEW_AGENT_ID) {
            AgentConfig(
                name = "Новый агент",
                model = AgentLlmModel.DeepSeekFlash,
                dialogTheme = "Рабочий помощник",
                systemPrompt = "Ты персональный AI-агент. Сначала уточняй недостающий контекст, затем давай короткий план и конкретный ответ."
            )
        } else {
            AgentConfig(
                name = "Визовый специалист",
                model = AgentLlmModel.DeepSeekFlash,
                dialogTheme = "Консультация по визам, документам и подготовке к подаче",
                systemPrompt = "Ты визовый специалист. Помогай пользователю разобраться с требованиями, документами, сроками, рисками отказа и подготовкой к подаче. Не выдавай юридические гарантии, отмечай, когда нужно проверить правила конкретной страны или обратиться к официальному источнику.\n\nКогда перечисляешь пакет документов, оформляй его отдельным блоком ровно в таком формате (одна строка — один документ, статус через точку с запятой, статусы только: нужен / загружен / проверен):\n[checklist]\n- Загранпаспорт; нужен\n- Фото 35×45; нужен\n[/checklist]\nОстальной текст пиши обычным образом до или после блока."
            )
        }
}

data class AgentChatUiState(
    val config: AgentConfig,
    val message: String = "",
    val messages: List<AgentChatMessage> = emptyList(),
    val conversations: List<Conversation> = emptyList(),
    val activeConversationId: Long? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val availableModels: List<AgentLlmModel> = AgentLlmModel.entries,
    /**
     * Локальная оценка токенов контекста, реально уходящего в модель: при включённом сжатии —
     * `summary + хвост`, иначе — вся история (+ черновик).
     */
    val contextTokens: Int = 0,
    /** Оценка полной истории без сжатия — для сравнения «сжато vs полная история». */
    val fullContextTokens: Int = 0,
    /** Эффективный лимит контекстного окна (демо-лимит или лимит модели). */
    val contextLimit: Int = 0,
    /** Сколько старых сообщений свёрнуто в summary (0 — сжатие не сработало/выключено). */
    val summarizedCount: Int = 0,
    /** Текст текущего summary (для карточки «сжатая память»); null — сжатия нет. */
    val summaryText: String? = null,
    /** Оценка размера summary в токенах (для подписи карточки). */
    val summaryTokens: Int = 0,
    /** Состояние демо-заливки контекста (фоновый авто-диалог). */
    val contextFill: ContextFillUiState = ContextFillUiState.Idle
) {
    /** Экономия токенов от сжатия в процентах (0, если сжатия нет или истории мало). */
    val compressionSavingsPercent: Int
        get() = if (summarizedCount > 0 && fullContextTokens > contextTokens && fullContextTokens > 0) {
            ((fullContextTokens - contextTokens) * 100) / fullContextTokens
        } else {
            0
        }
}
