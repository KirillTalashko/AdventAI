package com.example.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Один шрифт (системный), чёткая шкала Display / Title / Body / Label.
 * Заголовки короткие и плотные, тело — читаемое с увеличенным line-height.
 */
private val Default = Typography()

val AdventTypography = Default.copy(
    headlineLarge = Default.headlineLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = Default.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = Default.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = Default.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Default.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = Default.bodyLarge.copy(lineHeight = 23.sp),
    bodyMedium = Default.bodyMedium.copy(lineHeight = 20.sp),
    labelLarge = Default.labelLarge.copy(fontWeight = FontWeight.SemiBold)
)
