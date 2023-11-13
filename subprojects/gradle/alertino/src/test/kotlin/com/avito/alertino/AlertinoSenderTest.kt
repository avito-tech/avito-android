package com.avito.alertino

import com.avito.alertino.model.AlertinoRecipient
import com.avito.alertino.model.CreatedMessage
import com.avito.android.isFailure
import com.avito.android.isSuccess
import com.avito.logger.PrintlnLoggerFactory
import com.avito.test.http.MockWebServerFactory
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AlertinoSenderTest {

    private val mockWebServer = MockWebServerFactory.create()
    private val alertinoSender = AlertinoSenderFactory.create(mockWebServer.url("/").toString(), PrintlnLoggerFactory)
    private val threadId = "thread-id"

    private val errorResponse = MockResponse()
        .setResponseCode(405)
        .setBody("405 Not Allowed")

    private val correctResponse = MockResponse()
        .setResponseCode(200)
        .setBody("""
            {"result":{"createdMessages":{"username":"$threadId"},"alreadyExistingMessages":{},"creationErrors":{}}}
        """.trimIndent())

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun test() {
        mockWebServer.enqueue(errorResponse)
        val result = alertinoSender.sendNotification(
            template = "my-awesome-template",
            recipient = AlertinoRecipient("@username"),
            values = mapOf("placeholder" to "notification-text")
        )
        assertTrue(result.isFailure())
    }

    @Test
    fun test2() {
        mockWebServer.enqueue(correctResponse)
        val result = alertinoSender.sendNotification(
            template = "my-awesome-template",
            recipient = AlertinoRecipient("@username"),
            values = mapOf("placeholder" to "notification-text")
        )

        assertTrue(result.isSuccess())
        result.onSuccess { message ->
            assertEquals(CreatedMessage(AlertinoRecipient("@username"), threadId), message)
        }
    }
}
