package com.example.feature.chat.data.repository

import com.example.core.network.api.DeepSeekApi
import com.example.feature.chat.data.mapper.toChatRequestDto
import com.example.feature.chat.data.mapper.toResponseContent
import com.example.feature.chat.data.mapper.toUserChatMessage
import com.example.feature.chat.domain.exception.ChatException
import com.example.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val deepSeekApi: DeepSeekApi
) : ChatRepository {
    override suspend fun sendMessage(message: String): String = withContext(Dispatchers.IO) {
        try {
            val request = message.toUserChatMessage().toChatRequestDto()
            deepSeekApi.sendMessage(request).toResponseContent()
        } catch (exception: UnknownHostException) {
            throw ChatException(
                message = "Cannot resolve api.deepseek.com. Check internet, DNS, VPN or proxy settings.",
                cause = exception
            )
        } catch (exception: SocketTimeoutException) {
            throw ChatException(
                message = "Request timed out. Check your connection and try again.",
                cause = exception
            )
        } catch (exception: ConnectException) {
            throw ChatException(
                message = "Cannot connect to DeepSeek API. Check internet, VPN or proxy settings.",
                cause = exception
            )
        } catch (exception: SSLException) {
            throw ChatException(
                message = "Secure connection to DeepSeek failed: ${exception.safeMessage()}",
                cause = exception
            )
        } catch (exception: IOException) {
            throw ChatException(
                message = "Network error: ${exception.safeMessage()}",
                cause = exception
            )
        } catch (exception: HttpException) {
            throw ChatException(message = exception.toUiMessage(), cause = exception)
        } catch (exception: ChatException) {
            throw exception
        } catch (exception: Exception) {
            throw ChatException(
                message = exception.message ?: "Unexpected error. Try again later.",
                cause = exception
            )
        }
    }
}

private fun HttpException.toUiMessage(): String {
    val errorBody = response()
        ?.errorBody()
        ?.string()
        ?.takeIf(String::isNotBlank)
        ?.take(400)

    val baseMessage = when (code()) {
        400 -> "Invalid request format."
        401 -> "Authentication failed. Check DEEPSEEK_API_KEY in local.properties and rebuild the app."
        402 -> "Insufficient DeepSeek account balance."
        422 -> "Invalid request parameters."
        429 -> "Rate limit reached. Try again later."
        500 -> "DeepSeek server error. Try again later."
        503 -> "DeepSeek server is overloaded. Try again later."
        else -> "Server error ${code()}: ${message().ifBlank { "Try again later." }}"
    }

    return if (errorBody == null) {
        baseMessage
    } else {
        "$baseMessage Details: $errorBody"
    }
}

private fun Throwable.safeMessage(): String =
    localizedMessage?.takeIf(String::isNotBlank) ?: javaClass.simpleName
