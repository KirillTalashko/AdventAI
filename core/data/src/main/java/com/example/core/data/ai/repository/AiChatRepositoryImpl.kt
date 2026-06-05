package com.example.core.data.ai.repository

import com.example.core.common.AppError
import com.example.core.common.AppResult
import com.example.core.common.DefaultDispatcherProvider
import com.example.core.common.DispatcherProvider
import com.example.core.data.ai.mapper.Either
import com.example.core.data.ai.mapper.toChatRequestDto
import com.example.core.data.ai.mapper.toLlmAnswerOrError
import com.example.core.data.ai.mapper.toUserChatMessage
import com.example.core.domain.repository.AiChatRepository
import com.example.core.model.ai.ChatRequestOptions
import com.example.core.model.ai.LlmAnswer
import com.example.core.network.api.DeepSeekApi
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLException

class AiChatRepositoryImpl @Inject constructor(
    private val deepSeekApi: DeepSeekApi,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider
) : AiChatRepository {
    override suspend fun sendMessage(
        message: String,
        options: ChatRequestOptions
    ): AppResult<String> =
        when (val result = sendDetailedMessage(message = message, options = options)) {
            is AppResult.Success -> AppResult.Success(result.data.content)
            is AppResult.Error -> result
        }

    override suspend fun sendDetailedMessage(
        message: String,
        options: ChatRequestOptions
    ): AppResult<LlmAnswer> = withContext(dispatchers.io) {
        try {
            val request = message.toUserChatMessage().toChatRequestDto(options)

            when (val answer = deepSeekApi.sendMessage(request).toLlmAnswerOrError()) {
                is Either.Left -> AppResult.Error(answer.value)
                is Either.Right -> AppResult.Success(answer.value)
            }
        } catch (exception: UnknownHostException) {
            AppResult.Error(AppError.UnknownHost)
        } catch (exception: SocketTimeoutException) {
            AppResult.Error(AppError.Timeout)
        } catch (exception: ConnectException) {
            AppResult.Error(AppError.Connection)
        } catch (exception: SSLException) {
            AppResult.Error(AppError.SecureConnection(exception.safeMessage()))
        } catch (exception: IOException) {
            AppResult.Error(AppError.NetworkDetails(exception.safeMessage()))
        } catch (exception: HttpException) {
            AppResult.Error(exception.toAppError())
        } catch (exception: Exception) {
            AppResult.Error(AppError.Unknown(exception.message))
        }
    }
}

private fun HttpException.toAppError(): AppError {
    val details = response()
        ?.errorBody()
        ?.string()
        ?.takeIf(String::isNotBlank)
        ?.take(400)

    return when (code()) {
        400 -> AppError.InvalidRequest
        401 -> AppError.Unauthorized
        402 -> AppError.InsufficientBalance
        422 -> AppError.InvalidParameters
        429 -> AppError.RateLimit
        500 -> AppError.Server
        503 -> AppError.ServerOverloaded
        else -> AppError.ServerCode(
            code = code(),
            message = message().takeIf(String::isNotBlank),
            details = details
        )
    }
}

private fun Throwable.safeMessage(): String =
    localizedMessage?.takeIf(String::isNotBlank) ?: javaClass.simpleName
