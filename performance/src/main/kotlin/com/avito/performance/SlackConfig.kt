@file:Suppress("UnstableApiUsage")

package com.avito.performance

import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import java.io.Serializable

//todo use :slack module
data class SlackConfig(
    val hookUrl: String,
    val channel: String,
    val username: String,
    val avatar: String
) : Serializable


val Project.slackConfig: Provider<SlackConfig>
    get() = try {
        Providers.of(
            extensions.getByType<PerformanceExtension>().let { extension ->
                SlackConfig(
                    hookUrl = extension.slackHookUrl,
                    channel = extension.slackChannel,
                    username = extension.slackUsername,
                    avatar = extension.slackAvatar
                )
            }
        )
    } catch (e: Exception) {
        Providers.notDefined()
    }
