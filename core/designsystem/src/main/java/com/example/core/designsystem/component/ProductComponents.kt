package com.example.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.theme.AdventTheme
import com.example.core.designsystem.theme.AppRadii
import com.example.core.designsystem.theme.AppSpacing

/** Статус документа в пакете клиента: нужен → загружен → проверен. */
enum class DocumentStatus(val label: String) {
    Needed("Нужен"),
    Uploaded("Загружен"),
    Verified("Проверен")
}

data class ChecklistItemUi(
    val title: String,
    val status: DocumentStatus
)

/** Цвет/контейнер статуса из расширенных токенов дизайн-системы. */
@Composable
private fun DocumentStatus.colors(): Pair<Color, Color> {
    val status = AdventTheme.status
    return when (this) {
        DocumentStatus.Needed -> status.neutral to status.neutralContainer
        DocumentStatus.Uploaded -> status.info to status.infoContainer
        DocumentStatus.Verified -> status.success to status.successContainer
    }
}

@Composable
fun StatusPill(
    status: DocumentStatus,
    modifier: Modifier = Modifier
) {
    val (content, container) = status.colors()
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = container
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = content,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

/**
 * Карточка чек-листа документов. Используется и как «богатое сообщение» в чате,
 * и на вкладке «Документы». Никакого сырого markdown — пункты со статусами.
 */
@Composable
fun DocumentChecklistCard(
    items: List<ChecklistItemUi>,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.card),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
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
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    StatusPill(status = item.status)
                }
            }
        }
    }
}

/**
 * Кольцо прогресса готовности к подаче. Спокойная анимация не нужна на статике —
 * просто рисуем трек + дугу прогресса, в центр кладём произвольный контент.
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 132.dp,
    strokeWidth: Dp = 12.dp,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    content: @Composable BoxScope.() -> Unit
) {
    val safeProgress = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier.size(diameter),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(diameter)) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = androidx.compose.ui.geometry.Size(
                width = size.width - stroke,
                height = size.height - stroke
            )
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * safeProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        content()
    }
}
