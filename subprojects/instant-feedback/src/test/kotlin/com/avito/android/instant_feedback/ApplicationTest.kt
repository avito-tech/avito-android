package com.avito.android.instant_feedback

import com.avito.android.instant_feedback.internal.config.StubConfigFactory
import com.avito.android.instant_feedback.internal.model.createPayload
import com.avito.logger.PrintlnLoggerFactory
import com.google.common.truth.Truth.assertThat
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test

class ApplicationTest {

    @Test
    fun `health check - returns OK`() = testApplication {
        application {
            configureRouting(StubConfigFactory, PrintlnLoggerFactory)
        }

        client.get("/health").apply {
            assertThat(status).isEqualTo(HttpStatusCode.OK)
            assertThat(bodyAsText()).isEqualTo("""{"status":"ok"}""")
        }
    }

    @Test
    fun `pr merged - returns accepted`() = testApplication {
        application {
            configureRouting(StubConfigFactory, PrintlnLoggerFactory)
        }

        client.post("/webhooks/repository") {
            setBody(
                createPayload(
                    author = "amenethil",
                    authorEmail = "amenethil@avito.ru",
                    displayName = "Arthas Menethil",
                    title = "MBSA-45: Order Jaina to find Uther",
                    repository = "avito-android"
                )
            )
            header("X-Event-Key", "pr:merged")
        }.apply {
            assertThat(status).isEqualTo(HttpStatusCode.OK)
            assertThat(bodyAsText()).isEqualTo("""{"status":"accepted"}""")
        }
    }
}
