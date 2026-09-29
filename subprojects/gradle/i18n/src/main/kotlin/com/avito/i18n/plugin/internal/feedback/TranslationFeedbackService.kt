package com.avito.i18n.plugin.internal.feedback

import com.avito.android.feedback.InstantFeedbackEvent
import com.avito.android.feedback.InstantFeedbackService
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.util.concurrent.CopyOnWriteArrayList

internal abstract class TranslationFeedbackService :
    BuildService<TranslationFeedbackService.Params>,
    AutoCloseable {

    interface Params : BuildServiceParameters {
        val feedbackService: Property<InstantFeedbackService>
    }

    private val logger by lazy { Logging.getLogger(TranslationFeedbackService::class.java) }

    private val outcomes = CopyOnWriteArrayList<TranslationOutcome>()

    fun record(outcome: TranslationOutcome) {
        outcomes.add(outcome)
    }

    override fun close() {
        if (outcomes.isEmpty()) return

        try {
            sendPending()
        } finally {
            outcomes.clear()
        }
    }

    private fun sendPending() {
        val outcome = outcomes.firstOrNull { it.errorText != null }
            ?: outcomes.firstOrNull()
            ?: return

        val result = parameters.feedbackService.get().send(eventFromOutcome(outcome))

        result.onFailure { error ->
            if (outcome.errorText != null) {
                logger.warn("Instant feedback: event not sent: ${error.message}")
            } else {
                throw error
            }
        }
    }

    private fun eventFromOutcome(outcome: TranslationOutcome): InstantFeedbackEvent =
        InstantFeedbackEvent(
            scenario = SCENARIO,
            arguments = buildMap {
                put("instrumentName", INSTRUMENT_NAME)
                put("platform", PLATFORM)
                put("target", outcome.module)
                put("locales", outcome.locales.sorted().joinToString(", "))
                put("modules", outcomes.size.toString())
                if (outcome.errorText == null) {
                    put("status", "success")
                } else {
                    put("status", "failure")
                    put("error", outcome.errorText)
                }
            },
        )

    companion object {

        const val NAME: String = "com.avito.i18n.plugin.TranslationFeedbackService"

        private const val SCENARIO = "after_android_update_translations"
        private const val INSTRUMENT_NAME = "Gradle plugin"
        private const val PLATFORM = "android"

        fun provide(
            project: Project,
            feedbackService: Provider<InstantFeedbackService>,
        ): Provider<TranslationFeedbackService> {
            return project.gradle.sharedServices.registerIfAbsent(
                NAME,
                TranslationFeedbackService::class.java,
            ) {
                it.parameters { params ->
                    params.feedbackService.set(feedbackService)
                }
            }
        }
    }
}
