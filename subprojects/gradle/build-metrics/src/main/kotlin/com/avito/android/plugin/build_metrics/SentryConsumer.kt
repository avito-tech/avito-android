@file:Suppress("RemoveCurlyBracesFromTemplate")

package com.avito.android.plugin.build_metrics

import com.avito.android.gradle.metric.AbstractMetricsConsumer
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.sentry.sentry
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import io.sentry.connection.EventSendCallback
import io.sentry.event.Event
import org.gradle.BuildResult
import org.gradle.api.Project

// todo вынести в фабрику знания про project
internal class SentryConsumer(
    project: Project,
    loggerFactory: LoggerFactory
) : AbstractMetricsConsumer() {

    private val logger = loggerFactory.create<SentryConsumer>()

    private val sentry = project.sentry

    private val sentryUrl: String? = project.getOptionalStringProperty("avito.sentry.projectUrl")

    private val capturedOutputs = mutableSetOf<Int>()

    private var hasSendCallback = false

    override fun onOutput(output: CharSequence) {
        // Do not print or log here. It'll cause infinite recursion.
        if (isCaptured(output)) return
        if (output.contains("SentryConsumer")) return

        when {
            output.startsWith("Could not store entry ") -> handleCacheError(output)
            output.startsWith("Could not load entry ") -> handleCacheError(output)
        }
    }

    private fun handleCacheError(output: CharSequence) {
        recordCapture(output)
        sendException(GradleCacheError(output.toString()))
    }

    /**
     * To deal with duplicated messages
     */
    private fun isCaptured(output: CharSequence): Boolean {
        return capturedOutputs.contains(output.hashCode())
    }

    private fun recordCapture(output: CharSequence) {
        capturedOutputs.add(output.hashCode())
    }

    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
        val failure = buildResult.failure ?: return

        sendException(failure)
    }

    private fun sendException(error: Throwable) {
        if (!hasSendCallback) {
            sentry.get().addEventSendCallback(DefaultSendCallback(logger = logger, sentryUrl = sentryUrl))
            hasSendCallback = true
        }
        sentry.get().sendException(error)
    }
}

private class DefaultSendCallback(
    private val logger: Logger,
    private val sentryUrl: String?
) : EventSendCallback {

    override fun onSuccess(event: Event) {
        val hash = event.id.toString().replace("-", "")
        if (sentryUrl.isNullOrBlank()) {
            logger.info("Error sent")
        } else {
            logger.info("Error sent: $sentryUrl?query=$hash")
        }
    }

    override fun onFailure(event: Event, exception: java.lang.Exception) {
        val cause = exception.javaClass.simpleName +
            " ← " + exception.cause?.javaClass?.name +
            ": " + exception.cause?.message
        logger.warn("Can't send error: $cause. \nCheck your connection.")
    }
}

internal class GradleCacheError(message: String) : Exception(message)
