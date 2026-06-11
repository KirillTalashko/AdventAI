package com.example.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Шкала отступов 4 / 8 / 12 / 16 / 24 / 32 (SCREEN_REQUIREMENTS.md §2).
 * В Compose нет встроенных spacing-токенов — держим единый источник, чтобы экраны
 * не плодили «магические» dp вразнобой.
 */
object AppSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

/**
 * Единые радиусы форм. Карточки 20–28dp, поля/композер 24dp (pill), пузыри 20dp,
 * bottom sheet 28dp сверху, иконки — CircleShape (см. компоненты).
 */
object AppRadii {
    val bubble = 20.dp
    val card = 24.dp
    val cardLarge = 28.dp
    val field = 24.dp
    val sheet = 28.dp
}
