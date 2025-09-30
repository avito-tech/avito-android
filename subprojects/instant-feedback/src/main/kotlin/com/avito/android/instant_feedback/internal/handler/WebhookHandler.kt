package com.avito.android.instant_feedback.internal.handler

import com.avito.android.Result
import com.avito.android.instant_feedback.internal.client.FeedbackClient
import com.avito.android.instant_feedback.internal.client.RequestFeedbackData
import com.avito.android.instant_feedback.internal.model.PullRequestMergedEvent
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineInterceptor
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal class WebhookHandler(
    private val json: Json,
    private val feedbackClient: FeedbackClient,
    loggerFactory: LoggerFactory,
) {

    private val logger = loggerFactory.create<WebhookHandler>()

    val action: PipelineInterceptor<Unit, ApplicationCall> = {
        val eventType = call.request.header(HEADER_EVENT) ?: ""
        val payload = call.receiveText()

        call.respond(mapOf("status" to "accepted"))

        launch {
            processWebhookAsync(eventType, payload)
        }
    }

    internal suspend fun processWebhookAsync(eventType: String, payload: String) = Result.tryCatch {
        if (eventType == PR_MERGED_EVENT) {
            val event: PullRequestMergedEvent = json.decodeFromString(payload)
            processEvent(event)
        } else {
            logger.warn("Unsupported webhook event: $eventType")
        }
    }

    private suspend fun processEvent(event: PullRequestMergedEvent): Result<Unit> {
        logger.info("Processing PR merge event: $event")

        return getScenarioForRepository(event.pr.ref.repository.name)
            .flatMap { sendFeedback(it, event.user.username) }
            .onFailure { throwable ->
                logger.critical("Failed to process PullRequestMerged event: $event", throwable)
            }
    }

    private fun getScenarioForRepository(repositoryName: String): Result<String> = when (repositoryName) {
        "avito-android" -> Result.Success("android_ci_pr_merge_feedback")
        "avito-ios" -> Result.Success("ios_ci_pr_merge_feedback")
        else -> Result.Failure(RuntimeException("Repository $repositoryName is not supported for feedback processing"))
    }

    private suspend fun sendFeedback(scenario: String, username: String): Result<Unit> = Result.tryCatch {
        val requestData = RequestFeedbackData(
            scenario = scenario,
            user = username,
            arguments = emptyMap()
        )
        feedbackClient.sendRequest(requestData)
    }

    private companion object {
        private const val HEADER_EVENT = "X-Event-Key"
        private const val PR_MERGED_EVENT = "pr:merged"
    }
}
