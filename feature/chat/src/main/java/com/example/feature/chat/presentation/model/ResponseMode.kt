package com.example.feature.chat.presentation.model

import androidx.annotation.StringRes
import com.example.feature.chat.R

enum class ResponseMode(
    @param:StringRes val labelResId: Int
) {
    Regular(labelResId = R.string.mode_unrestricted),
    Structured(labelResId = R.string.mode_restricted)
}
