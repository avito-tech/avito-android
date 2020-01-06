package com.avito.android.sentry

import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import io.sentry.SentryClient
import io.sentry.SentryClientFactory
import io.sentry.connection.NoopConnection
import io.sentry.context.ThreadLocalContextManager
import org.gradle.api.Project
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

    companion object {

        fun from(project: Project): SentryConfig {
            return if (project.getBooleanProperty("avito.sentry.enabled")) {
                val info = project.environmentInfo().get()
                val tags = mutableMapOf<String, String>()
                tags["ide"] = info.isInvokedFromIde().toString()
                info.teamcityBuildId()?.also { id ->
                    tags["build_id"] = id
                }
                Enabled(
                    dsn = project.getMandatoryStringProperty("avito.sentry.dsn"),
                    environment = info.environment.publicName,
                    serverName = info.node ?: "unknown",
                    release = info.commit ?: "unknown",
                    tags = tags
                )
            } else {
                Disabled
            }
        }
    }
}
