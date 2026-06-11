package com.example.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Расширенные цвета статусов — то, чего нет в стандартном [androidx.compose.material3.ColorScheme]
 * (там есть только error). Используются точечно для статусов документов и готовности к подаче.
 * Контент рисуется самим «сильным» цветом поверх его container — контраст AA сохраняется в обеих темах.
 */
@Immutable
data class AdventStatusColors(
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val info: Color,
    val infoContainer: Color,
    val neutral: Color,
    val neutralContainer: Color
)

val LightStatusColors = AdventStatusColors(
    success = LightStatusSuccess,
    successContainer = LightStatusSuccessContainer,
    warning = LightStatusWarning,
    warningContainer = LightStatusWarningContainer,
    info = LightStatusInfo,
    infoContainer = LightStatusInfoContainer,
    neutral = LightStatusNeutral,
    neutralContainer = LightStatusNeutralContainer
)

val DarkStatusColors = AdventStatusColors(
    success = DarkStatusSuccess,
    successContainer = DarkStatusSuccessContainer,
    warning = DarkStatusWarning,
    warningContainer = DarkStatusWarningContainer,
    info = DarkStatusInfo,
    infoContainer = DarkStatusInfoContainer,
    neutral = DarkStatusNeutral,
    neutralContainer = DarkStatusNeutralContainer
)

val LocalAdventStatusColors = staticCompositionLocalOf { LightStatusColors }
