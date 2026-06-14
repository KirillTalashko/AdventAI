package com.example.feature.chat.presentation.compression.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.designsystem.theme.AdventTheme
import com.example.core.designsystem.theme.AppRadii
import com.example.core.designsystem.theme.AppSpacing
import com.example.feature.chat.R
import com.example.feature.chat.presentation.compression.model.AbRunResult
import com.example.feature.chat.presentation.compression.model.AbSummarizationEvent
import com.example.feature.chat.presentation.compression.model.AbVariant
import com.example.feature.chat.presentation.compression.model.CompressionAbUiState
import com.example.feature.chat.presentation.compression.viewmodel.CompressionAbViewModel
import java.util.Locale

@Composable
fun CompressionAbRoute(
    viewModel: CompressionAbViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    CompressionAbScreen(
        state = uiState.value,
        onRun = viewModel::runExperiment,
        onStop = viewModel::onStop,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun CompressionAbScreen(
    state: CompressionAbUiState,
    onRun: () -> Unit,
    onStop: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasData = state.baseline.contextSeries.isNotEmpty() || state.compressed.contextSeries.isNotEmpty()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Header(onNavigateBack = onNavigateBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
        ) {
            Text(
                text = stringResource(R.string.compression_ab_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            RunControl(state = state, onRun = onRun, onStop = onStop)

            if (hasData) {
                ComparisonTableCard(state = state)
            }
            if (state.baseline.contextSeries.size >= 2 || state.compressed.contextSeries.size >= 2) {
                ContextChartCard(state = state)
            }
            if (state.hasConclusion) {
                ConclusionCard(state = state)
            }
            if (state.compressed.summarizations.isNotEmpty()) {
                SummarizationTimelineCard(events = state.compressed.summarizations)
            }

            state.error?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppRadii.bubble),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(AppSpacing.lg),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))
        }
    }
}

@Composable
private fun Header(onNavigateBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable(onClick = onNavigateBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back_24),
                    contentDescription = stringResource(R.string.compression_ab_title),
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(R.string.compression_ab_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RunControl(
    state: CompressionAbUiState,
    onRun: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        val label = when {
            state.isRunning -> stringResource(R.string.compression_ab_stop)
            state.finished -> stringResource(R.string.compression_ab_rerun)
            else -> stringResource(R.string.compression_ab_run)
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppRadii.bubble))
                .clickable { if (state.isRunning) onStop() else onRun() },
            shape = RoundedCornerShape(AppRadii.bubble),
            color = if (state.isRunning) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.primary
            }
        ) {
            Row(
                modifier = Modifier.padding(AppSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (state.isRunning) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }
                )
            }
        }
        if (state.isRunning) {
            val variantName = when (state.activeVariant) {
                AbVariant.Baseline -> stringResource(R.string.compression_ab_variant_baseline)
                AbVariant.Compressed -> stringResource(R.string.compression_ab_variant_compressed)
                null -> ""
            }
            val active = if (state.activeVariant == AbVariant.Compressed) state.compressed else state.baseline
            Text(
                text = stringResource(R.string.compression_ab_running, variantName) +
                    " · " + stringResource(R.string.compression_ab_turns, active.turnsDone, state.turnsTotal),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        } else if (!state.finished) {
            Text(
                text = stringResource(R.string.compression_ab_idle_hint, state.turnsTotal.coerceAtLeast(12)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Таблица сравнения
// ---------------------------------------------------------------------------

@Composable
private fun ComparisonTableCard(
    state: CompressionAbUiState,
    modifier: Modifier = Modifier
) {
    val baseline = state.baseline
    val compressed = state.compressed
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.cardLarge),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = stringResource(R.string.compression_ab_table_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            CompareHeaderRow()
            CompareRow(
                label = stringResource(R.string.compression_ab_row_prompt),
                baseline = formatTokens(baseline.promptTokens),
                compressed = formatTokens(compressed.promptTokens)
            )
            CompareRow(
                label = stringResource(R.string.compression_ab_row_completion),
                baseline = formatTokens(baseline.completionTokens),
                compressed = formatTokens(compressed.completionTokens)
            )
            CompareRow(
                label = stringResource(R.string.compression_ab_row_total),
                baseline = formatTokens(baseline.totalTokens),
                compressed = formatTokens(compressed.totalTokens),
                emphasize = true
            )
            if (compressed.overheadTokens > 0) {
                CompareRow(
                    label = stringResource(R.string.compression_ab_row_overhead),
                    baseline = "—",
                    compressed = formatTokens(compressed.overheadTokens),
                    muted = true
                )
            }
            CompareRow(
                label = stringResource(R.string.compression_ab_row_peak),
                baseline = formatTokens(baseline.peakContextTokens),
                compressed = formatTokens(compressed.peakContextTokens),
                emphasize = true
            )
            CompareRow(
                label = stringResource(R.string.compression_ab_row_cache),
                baseline = formatTokens(baseline.cacheHitTokens),
                compressed = formatTokens(compressed.cacheHitTokens)
            )
            CompareRow(
                label = stringResource(R.string.compression_ab_row_cost_cache),
                baseline = formatUsd(baseline.costUsd),
                compressed = formatUsd(compressed.costUsd)
            )
            CompareRow(
                label = stringResource(R.string.compression_ab_row_cost_nocache),
                baseline = formatUsd(baseline.costNoCacheUsd),
                compressed = formatUsd(compressed.costNoCacheUsd)
            )
            NameRow(baseline = baseline.nameRecalled, compressed = compressed.nameRecalled)

            baseline.nameAnswer?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = stringResource(R.string.compression_ab_name_answer_baseline, it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            compressed.nameAnswer?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = stringResource(R.string.compression_ab_name_answer_compressed, it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CompareHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1.5f))
        HeaderCell(text = stringResource(R.string.compression_ab_col_baseline), color = MaterialTheme.colorScheme.error)
        HeaderCell(text = stringResource(R.string.compression_ab_col_compressed), color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.HeaderCell(text: String, color: Color) {
    Text(
        text = text,
        modifier = Modifier.weight(1f),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = color,
        textAlign = androidx.compose.ui.text.style.TextAlign.End,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun CompareRow(
    label: String,
    baseline: String,
    compressed: String,
    emphasize: Boolean = false,
    muted: Boolean = false
) {
    val valueStyle = if (emphasize) {
        MaterialTheme.typography.titleSmall
    } else {
        MaterialTheme.typography.bodyMedium
    }
    val baseColor = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = baseline,
            modifier = Modifier.weight(1f),
            style = valueStyle,
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal,
            color = baseColor,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            maxLines = 1
        )
        Text(
            text = compressed,
            modifier = Modifier.weight(1f),
            style = valueStyle,
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal,
            color = if (emphasize) MaterialTheme.colorScheme.primary else baseColor,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            maxLines = 1
        )
    }
}

@Composable
private fun NameRow(baseline: Boolean?, compressed: Boolean?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.compression_ab_row_name),
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        RecallCell(recalled = baseline)
        RecallCell(recalled = compressed)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.RecallCell(recalled: Boolean?) {
    val (text, color) = when (recalled) {
        true -> stringResource(R.string.compression_ab_name_yes) to AdventTheme.status.success
        false -> stringResource(R.string.compression_ab_name_no) to MaterialTheme.colorScheme.error
        null -> stringResource(R.string.compression_ab_name_pending) to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = text,
        modifier = Modifier.weight(1f),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = color,
        textAlign = androidx.compose.ui.text.style.TextAlign.End,
        maxLines = 1
    )
}

// ---------------------------------------------------------------------------
// График размера контекста (2 линии)
// ---------------------------------------------------------------------------

@Composable
private fun ContextChartCard(
    state: CompressionAbUiState,
    modifier: Modifier = Modifier
) {
    val baselineColor = MaterialTheme.colorScheme.error
    val compressedColor = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.cardLarge),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Text(
                text = stringResource(R.string.compression_ab_chart_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.compression_ab_chart_sub),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TwoLineChart(
                baseline = state.baseline.contextSeries,
                compressed = state.compressed.contextSeries,
                baselineColor = baselineColor,
                compressedColor = compressedColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
                LegendDot(color = baselineColor, label = stringResource(R.string.compression_ab_col_baseline))
                LegendDot(color = compressedColor, label = stringResource(R.string.compression_ab_col_compressed))
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun TwoLineChart(
    baseline: List<Int>,
    compressed: List<Int>,
    baselineColor: Color,
    compressedColor: Color,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppRadii.card),
        color = trackColor
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(AppSpacing.md)) {
            val maxValue = (
                (baseline.maxOrNull() ?: 0).coerceAtLeast(compressed.maxOrNull() ?: 0)
                ).coerceAtLeast(1).toFloat()

            fun drawSeries(points: List<Int>, color: Color) {
                if (points.size < 2) return
                val stepX = size.width / (points.size - 1).toFloat()
                val offsets = points.mapIndexed { index, value ->
                    Offset(stepX * index, size.height - (value / maxValue) * size.height)
                }
                val path = Path().apply {
                    moveTo(offsets.first().x, offsets.first().y)
                    offsets.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(path = path, color = color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                offsets.forEach { drawCircle(color = color, radius = 3.dp.toPx(), center = it) }
            }

            drawSeries(baseline, baselineColor)
            drawSeries(compressed, compressedColor)
        }
    }
}

// ---------------------------------------------------------------------------
// Итог
// ---------------------------------------------------------------------------

@Composable
private fun ConclusionCard(
    state: CompressionAbUiState,
    modifier: Modifier = Modifier
) {
    val qualityVerdict = when {
        state.baseline.nameRecalled == false && state.compressed.nameRecalled == true ->
            stringResource(R.string.compression_ab_quality_better)
        state.compressed.nameRecalled == false ->
            stringResource(R.string.compression_ab_quality_worse)
        else ->
            stringResource(R.string.compression_ab_quality_same)
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.cardLarge),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Text(
                text = stringResource(R.string.compression_ab_savings_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // 1. Качество
            ConclSection(title = stringResource(R.string.compression_ab_concl_quality)) {
                ConclLine(
                    text = stringResource(
                        R.string.compression_ab_concl_quality_baseline,
                        recallWord(state.baseline.nameRecalled)
                    ),
                    color = recallColor(state.baseline.nameRecalled)
                )
                ConclLine(
                    text = stringResource(
                        R.string.compression_ab_concl_quality_compressed,
                        recallWord(state.compressed.nameRecalled)
                    ),
                    color = recallColor(state.compressed.nameRecalled)
                )
                ConclLine(text = qualityVerdict, bold = true)
            }

            // 2. Токены (до → после)
            ConclSection(title = stringResource(R.string.compression_ab_concl_tokens)) {
                ConclBeforeAfter(
                    before = tokensText(state.baseline.totalTokens),
                    after = tokensText(state.compressed.totalTokens),
                    savedPercent = state.totalTokensSavedPercent
                )
            }

            // 3. Размер контекста (до → после)
            ConclSection(title = stringResource(R.string.compression_ab_concl_context)) {
                ConclBeforeAfter(
                    before = tokensText(state.baseline.peakContextTokens),
                    after = tokensText(state.compressed.peakContextTokens),
                    savedPercent = state.contextSavedPercent
                )
            }

            // 4. Деньги (с кэшем и без)
            ConclSection(title = stringResource(R.string.compression_ab_concl_money)) {
                ConclLine(
                    text = stringResource(
                        R.string.compression_ab_concl_money_cache,
                        formatUsd(state.baseline.costUsd),
                        formatUsd(state.compressed.costUsd)
                    )
                )
                ConclLine(
                    text = stringResource(
                        R.string.compression_ab_concl_money_nocache,
                        formatUsd(state.baseline.costNoCacheUsd),
                        formatUsd(state.compressed.costNoCacheUsd),
                        state.costNoCacheSavedPercent
                    )
                )
                if (state.compressionCostlierWithCache) {
                    Text(
                        text = stringResource(R.string.compression_ab_cache_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ConclSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        content()
    }
}

@Composable
private fun ConclLine(
    text: String,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    bold: Boolean = false
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        color = color
    )
}

@Composable
private fun ConclBeforeAfter(
    before: String,
    after: String,
    savedPercent: Int
) {
    ConclLine(text = stringResource(R.string.compression_ab_concl_before, before))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.compression_ab_concl_after, after),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        if (savedPercent > 0) {
            Text(
                text = stringResource(R.string.compression_ab_concl_saved, savedPercent),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AdventTheme.status.success,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun recallWord(recalled: Boolean?): String = when (recalled) {
    true -> stringResource(R.string.compression_ab_name_yes)
    false -> stringResource(R.string.compression_ab_name_no)
    null -> stringResource(R.string.compression_ab_name_pending)
}

@Composable
private fun recallColor(recalled: Boolean?): Color = when (recalled) {
    true -> AdventTheme.status.success
    false -> MaterialTheme.colorScheme.error
    null -> MaterialTheme.colorScheme.onPrimaryContainer
}

@Composable
private fun tokensText(value: Int): String =
    stringResource(R.string.compression_ab_concl_tokens_unit, formatTokens(value))

// ---------------------------------------------------------------------------
// Таймлайн свёрток
// ---------------------------------------------------------------------------

@Composable
private fun SummarizationTimelineCard(
    events: List<AbSummarizationEvent>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.cardLarge),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Text(
                text = stringResource(R.string.compression_ab_timeline_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.compression_ab_timeline_sub),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            events.forEachIndexed { index, event ->
                TimelineEvent(event = event, number = index + 1)
            }
        }
    }
}

@Composable
private fun TimelineEvent(
    event: AbSummarizationEvent,
    number: Int,
    modifier: Modifier = Modifier
) {
    var expanded by remember(event) { mutableStateOf(false) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.card),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.compression_ab_timeline_event,
                        event.afterTurn,
                        event.foldedCount,
                        event.totalSummarized
                    ),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                StoredBadge()
            }
            BeforeAfterBars(
                rawTokens = event.rawTokensBefore,
                summaryTokens = event.summaryTokens,
                savedPercent = event.savedPercent
            )
            Text(
                text = stringResource(R.string.compression_ab_timeline_folded, event.foldedPreview),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (expanded) {
                Text(
                    text = event.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = stringResource(
                    if (expanded) {
                        R.string.compression_ab_timeline_summary_hide
                    } else {
                        R.string.compression_ab_timeline_summary_show
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

@Composable
private fun BeforeAfterBars(
    rawTokens: Int,
    summaryTokens: Int,
    savedPercent: Int,
    modifier: Modifier = Modifier
) {
    val maxValue = rawTokens.coerceAtLeast(summaryTokens).coerceAtLeast(1)
    val rawFraction = (rawTokens.toFloat() / maxValue).coerceIn(0.02f, 1f)
    val summaryFraction = (summaryTokens.toFloat() / maxValue).coerceIn(0.02f, 1f)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        TokenBar(
            label = stringResource(R.string.compression_ab_timeline_before, formatTokens(rawTokens)),
            fraction = rawFraction,
            color = MaterialTheme.colorScheme.error
        )
        TokenBar(
            label = stringResource(R.string.compression_ab_timeline_after, formatTokens(summaryTokens)),
            fraction = summaryFraction,
            color = MaterialTheme.colorScheme.primary
        )
        if (savedPercent > 0) {
            Text(
                text = stringResource(R.string.compression_ab_timeline_ratio, savedPercent),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AdventTheme.status.success
            )
        }
    }
}

@Composable
private fun TokenBar(
    label: String,
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun StoredBadge() {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    ) {
        Text(
            text = stringResource(R.string.compression_ab_timeline_stored),
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1
        )
    }
}

private fun formatTokens(value: Int): String =
    String.format(Locale.getDefault(), "%,d", value)

private fun formatUsd(value: Double): String = when {
    value <= 0.0 -> "$0"
    value < 0.01 -> "$" + String.format(Locale.US, "%.5f", value)
    else -> "$" + String.format(Locale.US, "%.4f", value)
}
