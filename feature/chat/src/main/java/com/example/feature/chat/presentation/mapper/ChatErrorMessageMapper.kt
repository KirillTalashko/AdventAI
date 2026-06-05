package com.example.feature.chat.presentation.mapper

import com.example.core.common.AppError
import com.example.feature.chat.R
import com.example.feature.chat.presentation.text.ChatTextProvider
import javax.inject.Inject

class ChatErrorMessageMapper @Inject constructor(
    private val textProvider: ChatTextProvider
) {
    fun emptyMessage(): String =
        textProvider.get(R.string.error_enter_message)

    fun map(error: AppError): String = when (error) {
        AppError.EmptyPrompt -> emptyMessage()
        AppError.EmptyResponse -> textProvider.get(R.string.error_empty_response)
        AppError.TokenLimitReached -> textProvider.get(R.string.error_token_limit)
        AppError.UnknownHost -> textProvider.get(R.string.error_unknown_host)
        AppError.Timeout -> textProvider.get(R.string.error_timeout)
        AppError.Connection -> textProvider.get(R.string.error_connection)
        AppError.Network -> textProvider.get(R.string.error_connection)
        AppError.Unauthorized -> textProvider.get(R.string.error_authentication)
        AppError.RateLimit -> textProvider.get(R.string.error_rate_limit)
        AppError.InvalidRequest -> textProvider.get(R.string.error_invalid_request)
        AppError.InvalidParameters -> textProvider.get(R.string.error_invalid_parameters)
        AppError.InsufficientBalance -> textProvider.get(R.string.error_insufficient_balance)
        AppError.Server -> textProvider.get(R.string.error_server)
        AppError.ServerOverloaded -> textProvider.get(R.string.error_server_overloaded)
        is AppError.SecureConnection -> textProvider.get(
            R.string.error_secure_connection,
            error.details
        )

        is AppError.NetworkDetails -> textProvider.get(
            R.string.error_network,
            error.details
        )

        is AppError.ServerCode -> error.toUiMessage()
        is AppError.Unknown -> error.message ?: textProvider.get(R.string.error_unexpected)
    }

    private fun AppError.ServerCode.toUiMessage(): String {
        val baseMessage = textProvider.get(
            R.string.error_server_code,
            code,
            message ?: textProvider.get(R.string.error_try_again)
        )

        return details?.let { errorDetails ->
            textProvider.get(
                R.string.error_with_details,
                baseMessage,
                errorDetails
            )
        } ?: baseMessage
    }
}
