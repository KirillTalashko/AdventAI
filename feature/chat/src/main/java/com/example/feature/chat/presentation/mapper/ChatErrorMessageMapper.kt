package com.example.feature.chat.presentation.mapper

import com.example.feature.chat.R
import com.example.feature.chat.domain.exception.ChatException
import com.example.feature.chat.presentation.text.ChatTextProvider
import javax.inject.Inject

class ChatErrorMessageMapper @Inject constructor(
    private val textProvider: ChatTextProvider
) {
    fun emptyMessage(): String =
        textProvider.get(R.string.error_enter_message)

    fun map(throwable: Throwable): String = when (throwable) {
        is ChatException.EmptyResponse -> textProvider.get(R.string.error_empty_response)
        is ChatException.TokenLimitReached -> textProvider.get(R.string.error_token_limit)
        is ChatException.UnknownHost -> textProvider.get(R.string.error_unknown_host)
        is ChatException.Timeout -> textProvider.get(R.string.error_timeout)
        is ChatException.Connection -> textProvider.get(R.string.error_connection)
        is ChatException.SecureConnection -> textProvider.get(
            R.string.error_secure_connection,
            throwable.details
        )

        is ChatException.Network -> textProvider.get(
            R.string.error_network,
            throwable.details
        )

        is ChatException.Http -> throwable.toUiMessage()
        is ChatException.Unexpected -> throwable.details
            ?: textProvider.get(R.string.error_unexpected)

        else -> throwable.message ?: textProvider.get(R.string.error_unexpected)
    }

    private fun ChatException.Http.toUiMessage(): String {
        val baseMessage = when (code) {
            400 -> textProvider.get(R.string.error_invalid_request)
            401 -> textProvider.get(R.string.error_authentication)
            402 -> textProvider.get(R.string.error_insufficient_balance)
            422 -> textProvider.get(R.string.error_invalid_parameters)
            429 -> textProvider.get(R.string.error_rate_limit)
            500 -> textProvider.get(R.string.error_server)
            503 -> textProvider.get(R.string.error_server_overloaded)
            else -> textProvider.get(
                R.string.error_server_code,
                code,
                statusMessage ?: textProvider.get(R.string.error_try_again)
            )
        }

        return details?.let { errorDetails ->
            textProvider.get(
                R.string.error_with_details,
                baseMessage,
                errorDetails
            )
        } ?: baseMessage
    }
}
