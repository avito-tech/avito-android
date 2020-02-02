package com.avito.android.sentry

import io.sentry.SentryClient
import io.sentry.SentryClientFactory
import io.sentry.connection.NoopConnection
import io.sentry.context.ThreadLocalContextManager
import java.io.Serializable

fun sentryClient(config: SentryConfig): SentryClient {
    return when (config) {
        is SentryConfig.Disabled -> {
            SentryClient(NoopConnection(), ThreadLocalContextManager())
        }
        is SentryConfig.Enabled -> {
            SentryClientFactory.sentryClient(config.dsn).apply {
                environment = config.environment
                serverName = config.serverName
                release = config.release
                config.tags.forEach { (key, value) ->
                    addTag(key, value)
                }
            }
        }
    }
}

/**
 * Default config for SentryClient
 */
sealed class SentryConfig : Serializable {

    object Disabled : SentryConfig()

    data class Enabled(
        val dsn: String,
        val environment: String,
        val serverName: String,
        val release: String,
        val tags: Map<String, String>
    ) : SentryConfig()
}
