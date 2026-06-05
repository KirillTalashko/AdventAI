package com.example.core.domain.usecase

import com.example.core.common.AppError
import com.example.core.common.AppResult
import com.example.core.testing.FakeAiChatRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SendChatMessageUseCaseTest {
    private val repository = FakeAiChatRepository()
    private val useCase = SendChatMessageUseCase(repository)

    @Test
    fun `invoke trims prompt and returns successful answer`() = runTest {
        repository.returns(answer = com.example.core.model.ai.LlmAnswer(content = "Done"))

        val result = useCase(message = "  Hello  ")

        assertEquals(AppResult.Success("Done"), result)
        assertEquals(listOf("Hello"), repository.receivedMessages)
    }

    @Test
    fun `invoke returns error from repository`() = runTest {
        repository.returnsError(AppError.RateLimit)

        val result = useCase(message = "Hello")

        assertTrue(result is AppResult.Error)
        assertEquals(AppError.RateLimit, (result as AppResult.Error).error)
    }
}
