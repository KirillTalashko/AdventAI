package com.example.feature.home.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.theme.AdventAITheme
import com.example.core.designsystem.theme.AppRadii
import com.example.core.designsystem.theme.AppSpacing

@Composable
fun HomeScreen(
    onOpenAgent: (String) -> Unit,
    visaAgentId: String,
    newAgentId: String,
    modifier: Modifier = Modifier
) {
    val agents = listOf(
        AgentItem(
            id = visaAgentId,
            title = "Визовый специалист",
            subtitle = "Документы, сроки, риски отказа и подготовка к подаче",
            tag = "Консультации"
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = AppSpacing.lg,
            end = AppSpacing.lg,
            top = AppSpacing.sm,
            bottom = AppSpacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        item {
            HomeHeader(modifier = Modifier.padding(vertical = AppSpacing.md))
        }
        items(agents.size) { index ->
            val agent = agents[index]
            AgentCard(agent = agent, onClick = { onOpenAgent(agent.id) })
        }
        item {
            AddAgentCard(onClick = { onOpenAgent(newAgentId) })
        }
    }
}

@Composable
private fun HomeHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        Text(
            text = "Агенты",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "Выберите помощника или создайте нового под свою задачу.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AgentCard(
    agent: AgentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadii.cardLarge))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppRadii.cardLarge),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Avatar(initials = initialsOf(agent.title), size = 52.dp)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    Text(
                        text = agent.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Tag(text = agent.tag)
                }
                ChevronRight(color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = agent.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AddAgentCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadii.cardLarge))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppRadii.cardLarge),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(AppSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                PlusGlyph(color = MaterialTheme.colorScheme.onPrimary)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                Text(
                    text = "Добавить агента",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Роль, модель, системный промпт и тема диалога.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun Avatar(
    initials: String,
    size: Dp,
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
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1
        )
    }
}

@Composable
private fun Tag(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ChevronRight(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val strokeWidth = 2.4.dp.toPx()
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.4f, size.height * 0.26f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.66f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.4f, size.height * 0.74f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.66f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun PlusGlyph(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val stroke = 3.dp.toPx()
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height * 0.2f),
            end = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height * 0.8f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height / 2f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height / 2f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

private fun initialsOf(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> "AI"
        words.size == 1 -> words[0].take(2).uppercase()
        else -> (words[0].take(1) + words[1].take(1)).uppercase()
    }
}

private data class AgentItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val tag: String
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AdventAITheme {
        HomeScreen(
            onOpenAgent = {},
            visaAgentId = "visa",
            newAgentId = "new"
        )
    }
}
