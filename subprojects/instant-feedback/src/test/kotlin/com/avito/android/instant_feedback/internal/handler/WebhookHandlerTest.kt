@file:Suppress("MaxLineLength")
package com.avito.android.instant_feedback.internal.handler

import com.avito.android.instant_feedback.internal.client.FakeFeedbackClient
import com.avito.android.instant_feedback.internal.model.createPayload
import com.avito.logger.PrintlnLoggerFactory
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class WebhookHandlerTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    private lateinit var fakeFeedbackClient: FakeFeedbackClient
    private val loggerFactory = PrintlnLoggerFactory
    private lateinit var handler: WebhookHandler

    private val androidScenarioPayload = createPayload(
        author = "varian",
        authorEmail = "vwrynn@avito.ru",
        displayName = "Varian Wrynn",
        title = "MBSA-567: Unite the Alliance kingdoms",
        repository = "avito-android"
    )

    private val iosScenarioPayload = createPayload(
        author = "thrall",
        authorEmail = "twarchief@avito.ru",
        displayName = "Thrall",
        title = "MBSA-456: Restore the elements balance",
        repository = "avito-ios"
    )

    @BeforeEach
    fun setUp() {
        fakeFeedbackClient = FakeFeedbackClient()
        handler = WebhookHandler(
            json = json,
            feedbackClient = fakeFeedbackClient,
            loggerFactory = loggerFactory
        )
    }

    @Test
    fun `pr merged event for avito-android - sends android scenario feedback - when processing webhook`() = runTest {
        handler.processWebhookAsync("pr:merged", androidScenarioPayload)

        assertThat(fakeFeedbackClient.requests).hasSize(1)
        val request = fakeFeedbackClient.requests.first()
        assertThat(request.scenario).isEqualTo("android_ci_pr_merge_feedback")
        assertThat(request.user).isEqualTo("varian")
        assertThat(request.arguments).isEmpty()
    }

    @Test
    fun `pr merged event for avito-ios - sends ios scenario feedback - when processing webhook`() = runTest {
        handler.processWebhookAsync("pr:merged", iosScenarioPayload)

        assertThat(fakeFeedbackClient.requests).hasSize(1)
        val request = fakeFeedbackClient.requests.first()
        assertThat(request.scenario).isEqualTo("ios_ci_pr_merge_feedback")
        assertThat(request.user).isEqualTo("thrall")
        assertThat(request.arguments).isEmpty()
    }

    @Test
    fun `pr merged event for unsupported repository - no feedback sent - when processing webhook`() = runTest {
        val unsupportedPayload = createPayload(
            author = "uther",
            authorEmail = "uther@stormwind.org",
            displayName = "Uther the Lightbringer",
            title = "MBSA-789: Bring light to the undead",
            repository = "stormwind-repo"
        )

        handler.processWebhookAsync("pr:merged", unsupportedPayload)

        assertThat(fakeFeedbackClient.requests).isEmpty()
    }

    @Test
    fun `pr opened event - no feedback sent - when processing unsupported event type`() = runTest {
        handler.processWebhookAsync("pr:opened", androidScenarioPayload)

        assertThat(fakeFeedbackClient.requests).isEmpty()
    }

    @Test
    fun `malformed json payload - no feedback sent - when processing invalid json`() = runTest {
        val corruptedPayload = """{"eventKey": "pr:merged", "invalid": json"""

        handler.processWebhookAsync("pr:merged", corruptedPayload)

        assertThat(fakeFeedbackClient.requests).isEmpty()
    }

    @Test
    fun `missing required fields - no feedback sent - when processing incomplete payload`() = runTest {
        val incompletePayload = """{"eventKey": "pr:merged"}"""

        handler.processWebhookAsync("pr:merged", incompletePayload)

        assertThat(fakeFeedbackClient.requests).isEmpty()
    }

    @Test
    fun `feedback client failure - no requests sent - when client throws exception`() = runTest {
        fakeFeedbackClient.simulateFailure()

        handler.processWebhookAsync("pr:merged", androidScenarioPayload)

        assertThat(fakeFeedbackClient.requests).isEmpty()
    }

    @Test
    fun `empty event type - no feedback sent - when processing empty event header`() = runTest {
        handler.processWebhookAsync("", androidScenarioPayload)

        assertThat(fakeFeedbackClient.requests).isEmpty()
    }

    @Test
    fun `multiple sequential events - processes all valid events - when receiving multiple webhooks`() = runTest {
        handler.processWebhookAsync("pr:merged", androidScenarioPayload)
        handler.processWebhookAsync("pr:merged", iosScenarioPayload)

        assertThat(fakeFeedbackClient.requests).hasSize(2)
        assertThat(fakeFeedbackClient.requests[0].scenario).isEqualTo("android_ci_pr_merge_feedback")
        assertThat(fakeFeedbackClient.requests[1].scenario).isEqualTo("ios_ci_pr_merge_feedback")
    }
}
