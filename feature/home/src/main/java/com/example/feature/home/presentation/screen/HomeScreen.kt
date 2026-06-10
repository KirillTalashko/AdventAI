package com.example.feature.home.presentation.screen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun HomeScreen(
    onOpenAgent: (String) -> Unit,
    visaAgentId: String,
    newAgentId: String,
    modifier: Modifier = Modifier
) {
    val carouselState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = carouselState)
    val items = listOf(
        AgentCarouselItem.Agent(
            AgentPoster(
                id = visaAgentId,
                title = "Визовый специалист",
                subtitle = "Документы, сроки, риски отказа и подготовка к подаче",
                initials = "VS",
                tag = "Консультации",
                accent = MaterialTheme.colorScheme.primary,
                secondAccent = MaterialTheme.colorScheme.tertiary
            )
        ),
        AgentCarouselItem.AddAgent(id = newAgentId)
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)
                    )
                )
            )
    ) {
        val cardWidth = responsiveCardWidth(maxWidth)
        val cardHeight = responsiveCardHeight(maxHeight)
        val horizontalPadding = ((maxWidth - cardWidth) / 2f).coerceAtLeast(16.dp)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            HomeHeader(
                modifier = Modifier.padding(start = 20.dp, top = 18.dp, end = 20.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = carouselState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    bottom = 24.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(
                    items = items,
                    key = { _, item -> item.id }
                ) { index, item ->
                    val transform = centerTransformForItem(
                        itemIndex = index,
                        listState = carouselState
                    )

                    when (item) {
                        is AgentCarouselItem.Agent -> AgentPosterCard(
                            agent = item.agent,
                            width = cardWidth,
                            height = cardHeight,
                            transform = transform,
                            onClick = { onOpenAgent(item.agent.id) }
                        )

                        is AgentCarouselItem.AddAgent -> AddAgentPosterCard(
                            width = cardWidth,
                            height = cardHeight,
                            transform = transform,
                            onClick = { onOpenAgent(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Агенты",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
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
private fun AgentPosterCard(
    agent: AgentPoster,
    width: Dp,
    height: Dp,
    transform: CarouselItemTransform,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(34.dp)

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .carouselItemAnimation(transform)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.86f),
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)
                    )
                ),
                shape = shape
            )
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AgentPosterVisual(
                initials = agent.initials,
                accent = agent.accent,
                secondAccent = agent.secondAccent,
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .weight(1f)
            )
            AgentCardBody(
                agent = agent,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AddAgentPosterCard(
    width: Dp,
    height: Dp,
    transform: CarouselItemTransform,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(34.dp)

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .carouselItemAnimation(transform)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.tertiaryContainer
                    )
                ),
                shape
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.7f),
                shape = shape
            )
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp)
        ) {
            PosterPattern(
                accent = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    PlusGlyph(color = MaterialTheme.colorScheme.onPrimary)
                }
                Text(
                    text = "Добавить агента",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Роль, модель, системный промпт и тема диалога.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "Создать",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AgentPosterVisual(
    initials: String,
    accent: Color,
    secondAccent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(accent, secondAccent)
                ),
            )
    ) {
        PosterPattern(
            accent = Color.White.copy(alpha = 0.22f),
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = initials,
            modifier = Modifier
                .align(Alignment.Center)
                .background(
                    color = Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 34.dp, vertical = 24.dp),
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            fontWeight = FontWeight.Black
        )
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(18.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.26f)
        ) {
            Text(
                text = "Активен",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AgentCardBody(
    agent: AgentPoster,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        InfoChip(text = agent.tag)
        Text(
            text = agent.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = agent.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InfoChip(
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PosterPattern(
    accent: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = accent,
            radius = size.minDimension * 0.34f,
            center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.2f)
        )
        drawCircle(
            color = accent.copy(alpha = accent.alpha * 0.52f),
            radius = size.minDimension * 0.22f,
            center = androidx.compose.ui.geometry.Offset(size.width * 0.08f, size.height * 0.86f)
        )
        drawRoundRect(
            color = accent.copy(alpha = accent.alpha * 0.7f),
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.52f, size.height * 0.68f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.42f, size.height * 0.18f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(42f, 42f)
        )
    }
}

@Composable
private fun PlusGlyph(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(30.dp)) {
        val stroke = 5.dp.toPx()
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width / 2f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height),
            strokeWidth = stroke
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, size.height / 2f),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2f),
            strokeWidth = stroke
        )
    }
}

private fun centerTransformForItem(
    itemIndex: Int,
    listState: LazyListState
): CarouselItemTransform {
    val layoutInfo = listState.layoutInfo
    val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == itemIndex }
        ?: return CarouselItemTransform(scale = MinCardScale, offsetFraction = 0f)
    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
    val itemCenter = item.offset + item.size / 2f
    val signedDistance = itemCenter - viewportCenter
    val distance = abs(signedDistance)
    val viewportWidth = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
    val focus = (1f - distance / (viewportWidth * 0.5f)).coerceIn(0f, 1f)
    val offsetFraction = (signedDistance / (viewportWidth * 0.5f)).coerceIn(-1f, 1f)

    return CarouselItemTransform(
        scale = MinCardScale + (MaxCardScale - MinCardScale) * focus,
        offsetFraction = offsetFraction
    )
}

@Composable
private fun Modifier.carouselItemAnimation(
    transform: CarouselItemTransform
): Modifier {
    val focus = ((transform.scale - MinCardScale) / (MaxCardScale - MinCardScale)).coerceIn(0f, 1f)
    val animatedScale by animateFloatAsState(
        targetValue = transform.scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "carouselScale"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = cardAlpha(transform.scale),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "carouselAlpha"
    )
    val animatedLift by animateFloatAsState(
        targetValue = -18f * focus,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "carouselLift"
    )
    val animatedRotation by animateFloatAsState(
        targetValue = -transform.offsetFraction * 3.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "carouselRotation"
    )

    return graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
        alpha = animatedAlpha
        translationY = animatedLift
        rotationZ = animatedRotation
    }
}

private fun responsiveCardWidth(screenWidth: Dp): Dp = when {
    screenWidth < 360.dp -> screenWidth * 0.82f
    screenWidth < 600.dp -> screenWidth * 0.78f
    else -> 420.dp
}

private fun responsiveCardHeight(screenHeight: Dp): Dp = when {
    screenHeight < 620.dp -> screenHeight * 0.62f
    screenHeight < 760.dp -> screenHeight * 0.66f
    else -> 500.dp
}

private fun cardAlpha(scale: Float): Float =
    0.78f + ((scale - MinCardScale) / (MaxCardScale - MinCardScale)) * 0.22f

private const val MinCardScale = 0.86f
private const val MaxCardScale = 1f

private data class CarouselItemTransform(
    val scale: Float,
    val offsetFraction: Float
)

private sealed interface AgentCarouselItem {
    val id: String

    data class Agent(val agent: AgentPoster) : AgentCarouselItem {
        override val id: String = agent.id
    }

    data class AddAgent(override val id: String) : AgentCarouselItem
}

private data class AgentPoster(
    val id: String,
    val title: String,
    val subtitle: String,
    val initials: String,
    val tag: String,
    val accent: Color,
    val secondAccent: Color
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            onOpenAgent = {},
            visaAgentId = "visa",
            newAgentId = "new"
        )
    }
}
