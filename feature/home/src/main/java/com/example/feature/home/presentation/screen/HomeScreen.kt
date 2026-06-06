package com.example.feature.home.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.component.SectionTitle
import com.example.feature.home.R

@Composable
fun HomeScreen(
    onOpenChat: () -> Unit,
    onOpenDay2Format: () -> Unit,
    onOpenDay3Reasoning: () -> Unit,
    onOpenDay4Temperature: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modules = listOf(
        LearningModule(
            title = stringResource(R.string.home_chat_title),
            description = stringResource(R.string.home_chat_description),
            onClick = onOpenChat
        ),
        LearningModule(
            title = stringResource(R.string.home_format_title),
            description = stringResource(R.string.home_format_description),
            onClick = onOpenDay2Format
        ),
        LearningModule(
            title = stringResource(R.string.home_reasoning_title),
            description = stringResource(R.string.home_reasoning_description),
            onClick = onOpenDay3Reasoning
        ),
        LearningModule(
            title = stringResource(R.string.home_temperature_title),
            description = stringResource(R.string.home_temperature_description),
            onClick = onOpenDay4Temperature
        )
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(text = stringResource(R.string.home_title))
        }
        items(modules) { module ->
            LearningModuleCard(module = module)
        }
    }
}

@Composable
private fun LearningModuleCard(
    module: LearningModule,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = module.onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = module.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = module.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class LearningModule(
    val title: String,
    val description: String,
    val onClick: () -> Unit
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            onOpenChat = {},
            onOpenDay2Format = {},
            onOpenDay3Reasoning = {},
            onOpenDay4Temperature = {}
        )
    }
}
