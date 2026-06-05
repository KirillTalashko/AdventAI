package com.example.feature.chat.presentation.text

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ChatTextProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun get(
        @StringRes resId: Int,
        vararg formatArgs: Any
    ): String = context.getString(resId, *formatArgs)
}
