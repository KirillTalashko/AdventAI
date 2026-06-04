package com.example.feature.chat.data.repository

import com.example.core.network.api.DeepSeekApi
import com.example.feature.chat.data.mapper.toChatRequestDto
import com.example.feature.chat.data.mapper.toResponseContent
import com.example.feature.chat.data.mapper.toUserChatMessage
import com.example.feature.chat.domain.exception.ChatException
import com.example.feature.chat.domain.model.ChatRequestOptions
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
    override suspend fun sendMessage(
        message: String,
        options: ChatRequestOptions
    ): String = withContext(Dispatchers.IO) {
        try {
            val request = message.toUserChatMessage().toChatRequestDto(options)
            deepSeekApi.sendMessage(request).toResponseContent()
        } catch (exception: UnknownHostException) {
            throw ChatException.UnknownHost(exception)
        } catch (exception: SocketTimeoutException) {
            throw ChatException.Timeout(exception)
        } catch (exception: ConnectException) {
            throw ChatException.Connection(exception)
        } catch (exception: SSLException) {
            throw ChatException.SecureConnection(
                details = exception.safeMessage(),
                cause = exception
            )
        } catch (exception: IOException) {
            throw ChatException.Network(
                details = exception.safeMessage(),
                cause = exception
            )
        } catch (exception: HttpException) {
            throw ChatException.Http(
                code = exception.code(),
                statusMessage = exception.message().takeIf(String::isNotBlank),
                details = exception.errorBody(),
                cause = exception
            )
        } catch (exception: ChatException) {
            throw exception
        } catch (exception: Exception) {
            throw ChatException.Unexpected(
                details = exception.message,
                cause = exception
            )
        }
    }
}

private fun HttpException.errorBody(): String? =
    response()
        ?.errorBody()
        ?.string()
        ?.takeIf(String::isNotBlank)
        ?.take(400)

private fun Throwable.safeMessage(): String =
    localizedMessage?.takeIf(String::isNotBlank) ?: javaClass.simpleName
