package com.example.adventai.ui.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.core.designsystem.component.ChecklistItemUi
import com.example.core.designsystem.component.DocumentChecklistCard
import com.example.core.designsystem.component.DocumentStatus
import com.example.core.designsystem.theme.AppSpacing

private val SampleDocuments = listOf(
    ChecklistItemUi("Загранпаспорт (срок > 6 мес.)", DocumentStatus.Verified),
    ChecklistItemUi("Фото 35×45 мм", DocumentStatus.Uploaded),
    ChecklistItemUi("Справка с работы", DocumentStatus.Uploaded),
    ChecklistItemUi("Выписка со счёта", DocumentStatus.Needed),
    ChecklistItemUi("Бронь авиабилетов", DocumentStatus.Needed),
    ChecklistItemUi("Медицинская страховка", DocumentStatus.Needed)
)

@Composable
fun DocumentsScreen(modifier: Modifier = Modifier) {
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
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                Text(
                    text = "Документы",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Пакет для туристической визы. Статусы обновляются по ходу диалога с агентом.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        item {
            DocumentChecklistCard(
                items = SampleDocuments,
                title = "Требуемые документы"
            )
        }
    }
}
