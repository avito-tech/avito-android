package com.avito.android.feedback

import com.avito.android.Result
import com.avito.android.feedback.internal.FeedbackSender
import com.avito.android.feedback.internal.RequestFeedbackData
import com.avito.android.tls.TlsConfigurationPlugin
import com.avito.android.tls.TlsCredentialsService
import com.avito.android.tls.credentials.TlsCredentials
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

public abstract class InstantFeedbackService : BuildService<InstantFeedbackService.Params> {

    public interface Params : BuildServiceParameters {
        public val enabled: Property<Boolean>
        public val dryRun: Property<Boolean>
        public val endpoint: Property<String>
        public val tlsCredentialsService: Property<TlsCredentialsService>
    }

    private val logger by lazy { Logging.getLogger(InstantFeedbackService::class.java) }

    private val sender by lazy { FeedbackSender(parameters.endpoint.get()) }

    private val usernameResolver: UsernameResolver by lazy {
        CertificateUserResolver(certificatePem = { readCertificatePem() })
    }

    private val user: Result<String> by lazy { usernameResolver.resolve() }

    public fun send(event: InstantFeedbackEvent): Result<Unit> {
        if (!parameters.enabled.get()) {
            logger.info("Instant feedback: disabled, event '${event.scenario}' dropped")
            return Result.Success(Unit)
        }
        if (parameters.dryRun.get()) {
            logger.info("Instant feedback: dry run, event '${event.scenario}' not sent")
            return Result.Success(Unit)
        }

        return user
            .rescue { error ->
                Result.Failure(
                    IllegalStateException(
                        "Instant feedback: can't resolve avito login from the mTLS certificate " +
                            "(${error.message}). Check that a valid personal certificate is available " +
                            "(run `avito login`), or turn the feature off with -P$ENABLED_PROPERTY=false",
                        error
                    )
                )
            }
            .flatMap { login -> doSend(event, login) }
    }

    private fun doSend(event: InstantFeedbackEvent, login: String): Result<Unit> {
        val data = RequestFeedbackData(
            scenario = event.scenario,
            user = login,
            arguments = event.arguments,
            date = Instant.now().atOffset(ZoneOffset.UTC).format(dateFormatter),
        )
        return sender.send(data)
            .onSuccess { logger.info("Instant feedback: '${event.scenario}' sent") }
            .rescue { error ->
                Result.Failure(
                    IllegalStateException(
                        "Instant feedback: failed to send '${event.scenario}' " +
                            "to ${parameters.endpoint.get()}: ${error.message}. " +
                            "Turn the feature off with -P$ENABLED_PROPERTY=false if it blocks you",
                        error
                    )
                )
            }
    }

    private fun readCertificatePem(): Result<String> =
        Result.tryCatch { parameters.tlsCredentialsService.get().createCredentials() }
            .flatMap { credentials ->
                when (credentials) {
                    is TlsCredentials.Failure -> Result.Failure(IllegalStateException(credentials.message))
                    else -> Result.tryCatch { credentials.crt }
                }
            }

    public companion object {

        public val NAME: String = InstantFeedbackService::class.java.name

        private const val ENABLED_PROPERTY = "avito.instantFeedback.enabled"
        private const val DRY_RUN_PROPERTY = "avito.instantFeedback.dryRun"
        private const val ENDPOINT_PROPERTY = "avito.instantFeedback.endpoint"

        private val dateFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT)

        public fun provide(project: Project): Provider<InstantFeedbackService> {
            return project.gradle.sharedServices.registerIfAbsent(NAME, InstantFeedbackService::class.java) {
                it.parameters { params ->
                    params.enabled.set(project.isFeedbackEnabled())
                    params.dryRun.set(project.getOptionalStringProperty(DRY_RUN_PROPERTY).toBoolean())
                    params.endpoint.set(project.feedbackEndpoint())
                    params.tlsCredentialsService.set(TlsConfigurationPlugin.provideCredentialsService(project))
                }
            }
        }

        private fun Project.isFeedbackEnabled(): Boolean {
            if (feedbackEndpoint() == null) return false

            val explicit = getOptionalStringProperty(ENABLED_PROPERTY, nullIfBlank = true)
            return explicit?.toBoolean() ?: buildEnvironment.isInteractive()
        }

        private fun Project.feedbackEndpoint(): String? =
            getOptionalStringProperty(ENDPOINT_PROPERTY, nullIfBlank = true)

        private fun BuildEnvironment.isInteractive(): Boolean =
            this is BuildEnvironment.Local || this is BuildEnvironment.IDE
    }
}
