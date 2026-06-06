package com.example.feature.chat.presentation.temperature.model

import androidx.annotation.StringRes
import com.example.feature.chat.R

enum class TemperatureMode(
    val value: Double,
    @StringRes val titleResId: Int,
    @StringRes val labelResId: Int
) {
    Precise(
        value = 0.0,
        titleResId = R.string.temperature_precise_title,
        labelResId = R.string.temperature_precise_label
    ),
    Balanced(
        value = 0.7,
        titleResId = R.string.temperature_balanced_title,
        labelResId = R.string.temperature_balanced_label
    ),
    Creative(
        value = 1.2,
        titleResId = R.string.temperature_creative_title,
        labelResId = R.string.temperature_creative_label
    )
}
